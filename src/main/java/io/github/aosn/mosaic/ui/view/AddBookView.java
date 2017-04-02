/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.domain.service.stock.StockService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.LoginRequiredLabel;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Notifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.time.LocalDate;

/**
 * A {@link View} for adding a book.
 * <p>
 * <p>Required session parameter:</p>
 * <ul>
 * <li>{@link #ATTR_BOOK_ADD} - A {@link ReleasedBook} object</li>
 * </ul>
 *
 * @author mikan
 * @since 0.3
 */
@SpringView(name = AddBookView.VIEW_NAME, ui = MainUI.class)
@Slf4j
public class AddBookView extends CustomComponent implements View {

    public static final String VIEW_NAME = "add-book";
    public static final String ATTR_BOOK_ADD = "mosaic.book.add";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final StockService stockService;
    private final VaadinSession session;

    @Autowired
    public AddBookView(I18N i18n, UserService userService, PollService pollService, StockService stockService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.stockService = stockService;
        session = VaadinSession.getCurrent();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        ReleasedBook releasedBook = (ReleasedBook) session.getAttribute(ATTR_BOOK_ADD);
        if (releasedBook == null) {
            ErrorView.show(i18n.get("common.error.parameter.missing"), null);
            return;
        }
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(),
                createAddStockLayout(Stock.create(userService.getUser(), releasedBook))));
    }

    private Layout createAddStockLayout(Stock book) {
        ContentPane contentPane = new ContentPane();

        FormLayout form = new FormLayout();
        form.setCaption(i18n.get("add-book.label.title"));
        form.setMargin(false);
        Binder<Stock> bookBinder = new Binder<>();
        bookBinder.readBean(book);

        Image thumbnail = new Image(book.getTitle(), new ExternalResource(book.getThumbnailOrPlaceholder()));
        thumbnail.setCaption(i18n.get("book.column.cover"));
        form.addComponent(thumbnail);

        Label isbnLabel = new Label(book.getIsbn());
        isbnLabel.setCaption(i18n.get("book.column.isbn"));
        form.addComponent(isbnLabel);

        TextField titleField = new TextField(i18n.get("book.column.title"));
        titleField.setRequiredIndicatorVisible(true);
        titleField.setWidth(100, Unit.PERCENTAGE);
        titleField.setValue(book.getTitle());
        bookBinder.forField(titleField)
                .withValidator(new StringLengthValidator(i18n.get("common.validator.text.length.over"), 0, 200))
                .bind(Stock::getTitle, Stock::setTitle);
        form.addComponent(titleField);

        TextField publishedDateField = new TextField(i18n.get("book.column.published.date"));
        publishedDateField.setRequiredIndicatorVisible(true);
        publishedDateField.setValue(book.getPublishedDate());
        bookBinder.forField(publishedDateField)
                .withValidator((v, c) -> Stock.isValidPublishedDate(v) ? ValidationResult.ok() :
                        ValidationResult.error(i18n.get("edit-book.validator.published.date")))
                .bind(Stock::getPublishedDate, Stock::setPublishedDate);
        form.addComponent(publishedDateField);

        TextField pagesField = new TextField(i18n.get("book.column.page.count"));
        pagesField.setValue(String.valueOf(book.getPageCount()));
        bookBinder.forField(pagesField)
                .withConverter(new StringToIntegerConverter(i18n.get("common.validator.date.range.over")))
                .withValidator((v, c) -> v >= 0 ? ValidationResult.ok() :
                        ValidationResult.error(i18n.get("common.validator.date.range.over")))
                .bind(Stock::getPageCount, Stock::setPageCount);
        form.addComponent(pagesField);

        TextField commentField = new TextField(i18n.get("book.column.text.short"));
        commentField.setWidth(100, Unit.PERCENTAGE);
        commentField.setValue(book.getShortText());
        bookBinder.forField(commentField).bind(Stock::getShortText, Stock::setShortText);
        form.addComponent(commentField);

        TextArea bookReviewTextArea = new TextArea(i18n.get("book.column.text.long"));
        bookReviewTextArea.setWidth(100, Unit.PERCENTAGE);
        bookReviewTextArea.setDescription(i18n.get("edit-book.description.text.long"));
        bookReviewTextArea.setValue(book.getLongText());
        bookBinder.forField(bookReviewTextArea).bind(Stock::getLongText, Stock::setLongText);
        form.addComponent(bookReviewTextArea);

        ComboBox<Stock.Visibility> visibilityComboBox = new ComboBox<>(i18n.get("book.column.visibility"));
        visibilityComboBox.setTextInputAllowed(false);
        visibilityComboBox.setEmptySelectionAllowed(false);
        visibilityComboBox.setItems(Stock.Visibility.values());
        visibilityComboBox.setValue(book.getVisibility());
        bookBinder.forField(visibilityComboBox).bind(Stock::getVisibility, Stock::setVisibility);
        form.addComponent(visibilityComboBox);

        ComboBox<Stock.Progress> progressComboBox = new ComboBox<>(i18n.get("book.column.progress"));
        progressComboBox.setTextInputAllowed(false);
        progressComboBox.setEmptySelectionAllowed(false);
        progressComboBox.setItems(Stock.Progress.values());
        progressComboBox.setValue(book.getProgress());
        bookBinder.forField(progressComboBox).bind(Stock::getProgress, Stock::setProgress);
        form.addComponent(progressComboBox);

        ComboBox<Stock.ObtainType> obtainTypeComboBox = new ComboBox<>(i18n.get("book.column.obtain.type"));
        obtainTypeComboBox.setTextInputAllowed(false);
        obtainTypeComboBox.setEmptySelectionAllowed(false);
        obtainTypeComboBox.setItems(Stock.ObtainType.values());
        obtainTypeComboBox.setValue(book.getObtainType());
        bookBinder.forField(obtainTypeComboBox).bind(Stock::getObtainType, Stock::setObtainType);
        form.addComponent(obtainTypeComboBox);

        DateField obtainDateField = new DateField(i18n.get("book.column.obtain.date"));
        bookBinder.forField(obtainDateField).bind(Stock::getObtainDateAsLocalDate, Stock::setObtainDate);
        form.addComponent(obtainDateField);

        DateField completeDateField = new DateField(i18n.get("book.column.progress.date.completed"));
        completeDateField.setRangeEnd(LocalDate.now());
        bookBinder.forField(completeDateField).bind(Stock::getCompletedDateAsLocalDate, Stock::setCompletedDate);
        form.addComponent(completeDateField);

        ComboBox<Stock.MediaType> mediaTypeComboBox = new ComboBox<>(i18n.get("book.column.media.type"));
        mediaTypeComboBox.setTextInputAllowed(false);
        mediaTypeComboBox.setEmptySelectionAllowed(false);
        mediaTypeComboBox.setItems(Stock.MediaType.values());
        mediaTypeComboBox.setValue(book.getMediaType());
        bookBinder.forField(mediaTypeComboBox).bind(Stock::getMediaType, Stock::setMediaType);
        form.addComponent(mediaTypeComboBox);

        TextField boughtPlaceField = new TextField(i18n.get("book.column.bought.place"));
        bookBinder.forField(boughtPlaceField)
                .withValidator(new StringLengthValidator(i18n.get("common.validator.text.length.over"), 0, 128))
                .bind(Stock::getBoughtPlace, Stock::setBoughtPlace);
        form.addComponent(boughtPlaceField);

        contentPane.addComponent(form);

        Button cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(FindBookView.VIEW_NAME));

        Button submitButton = new Button(i18n.get("add-book.button.submit"), e -> {
            // Validate
            if (!bookBinder.writeBeanIfValid(book)) {
                Notifications.showWarning(i18n.get("common.notification.input.incomplete"));
                return;
            }

            // Submit
            try {
                stockService.add(book);
            } catch (RuntimeException ex) {
                ErrorView.show(i18n.get("add-book.error.add.failed"), ex);
                return;
            }

            // Next
            Notifications.showSuccess(i18n.get("add-book.notification.add.success"));
            session.setAttribute(ATTR_BOOK_ADD, null); // clear session attribute
            getUI().getNavigator().navigateTo(BooksView.VIEW_NAME);
        });
        submitButton.setIcon(VaadinIcons.CHECK);
        submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

        HorizontalLayout buttonArea = new HorizontalLayout(cancelButton, submitButton);
        buttonArea.setSpacing(true);
        contentPane.addComponent(buttonArea);

        if (!userService.isLoggedIn()) {
            submitButton.setEnabled(false);
            contentPane.addComponent(new LoginRequiredLabel(i18n));
        }

        return contentPane;
    }
}
