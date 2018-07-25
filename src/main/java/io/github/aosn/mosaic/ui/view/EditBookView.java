/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
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
import org.vaadin.spring.i18n.I18N;

import java.time.LocalDate;
import java.util.Date;

/**
 * A {@link View} for adding a book.
 * <p>
 * <p>Required session parameter:</p>
 * <ul>
 * <li>{@link #ATTR_BOOK_EDIT} - A {@link Stock} object</li>
 * </ul>
 *
 * @author mikan
 * @since 0.3
 */
@SpringView(name = EditBookView.VIEW_NAME, ui = MainUI.class)
@Slf4j
public class EditBookView extends CustomComponent implements View {

    public static final String VIEW_NAME = "edit-book";
    static final String ATTR_BOOK_EDIT = "mosaic.book.edit";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final StockService stockService;
    private final VaadinSession session;

    public EditBookView(I18N i18n, UserService userService, PollService pollService, StockService stockService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.stockService = stockService;
        session = VaadinSession.getCurrent();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        var stock = (Stock) session.getAttribute(ATTR_BOOK_EDIT);
        if (stock == null) {
            ErrorView.show(i18n.get("common.error.parameter.missing"), null);
            return;
        }
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(),
                createEditStockLayout(stock)));
    }

    private Layout createEditStockLayout(Stock book) {
        var contentPane = new ContentPane();

        var form = new FormLayout();
        form.setMargin(false);
        contentPane.addComponent(form);
        var bookBinder = new Binder<Stock>();
        bookBinder.readBean(book);

        var thumbnail = new Image(book.getTitle(), new ExternalResource(book.getThumbnailOrPlaceholder()));
        thumbnail.setCaption(i18n.get("book.column.cover"));
        form.addComponent(thumbnail);

        var isbnLabel = new Label(book.getIsbn());
        isbnLabel.setCaption(i18n.get("book.column.isbn"));
        form.addComponent(isbnLabel);

        var titleField = new TextField(i18n.get("book.column.title"));
        titleField.setRequiredIndicatorVisible(true);
        titleField.setValue(book.getTitle());
        titleField.setWidth(100, Unit.PERCENTAGE);
        bookBinder.forField(titleField)
                .withValidator(new StringLengthValidator("Text is too long", 0, 200))
                .bind(Stock::getTitle, Stock::setTitle);
        form.addComponent(titleField);

        var publishedDateField = new TextField(i18n.get("book.column.published.date"));
        publishedDateField.setRequiredIndicatorVisible(true);
        publishedDateField.setValue(book.getPublishedDate());
        bookBinder.forField(publishedDateField)
                .withValidator((v, c) -> Stock.isValidPublishedDate(v) ? ValidationResult.ok() :
                        ValidationResult.error(i18n.get("edit-book.validator.published.date")))
                .bind(Stock::getPublishedDate, Stock::setPublishedDate);
        form.addComponent(publishedDateField);

        var pagesField = new TextField(i18n.get("book.column.page.count"));
        pagesField.setValue(String.valueOf(book.getPageCount()));
        bookBinder.forField(pagesField)
                .withConverter(new StringToIntegerConverter(i18n.get("common.validator.date.range.over")))
                .withValidator((v, c) -> v != null && v >= 0 ? ValidationResult.ok() :
                        ValidationResult.error(i18n.get("common.validator.date.range.over")))
                .bind(Stock::getPageCount, Stock::setPageCount);
        form.addComponent(pagesField);

        var commentField = new TextField(i18n.get("book.column.text.short"), book.getShortText());
        commentField.setWidth(100, Unit.PERCENTAGE);
        bookBinder.forField(commentField).bind(Stock::getShortText, Stock::setShortText);
        form.addComponent(commentField);

        var bookReviewTextArea = new TextArea(i18n.get("book.column.text.long"), book.getLongText());
        bookReviewTextArea.setWidth(100, Unit.PERCENTAGE);
        bookReviewTextArea.setDescription(i18n.get("edit-book.description.text.long")); // markdown available
        bookBinder.forField(bookReviewTextArea).bind(Stock::getLongText, Stock::setLongText);
        form.addComponent(bookReviewTextArea);

        var visibilityComboBox = new ComboBox<Stock.Visibility>(i18n.get("book.column.visibility"));
        visibilityComboBox.setTextInputAllowed(false);
        visibilityComboBox.setEmptySelectionAllowed(false);
        visibilityComboBox.setItems(Stock.Visibility.values());
        visibilityComboBox.setValue(book.getVisibility());
        bookBinder.forField(visibilityComboBox).bind(Stock::getVisibility, Stock::setVisibility);
        form.addComponent(visibilityComboBox);

        var progressComboBox = new ComboBox<Stock.Progress>(i18n.get("book.column.progress"));
        progressComboBox.setTextInputAllowed(false);
        progressComboBox.setEmptySelectionAllowed(false);
        progressComboBox.setItems(Stock.Progress.values());
        progressComboBox.setValue(book.getProgress());
        bookBinder.forField(progressComboBox).bind(Stock::getProgress, Stock::setProgress);
        form.addComponent(progressComboBox);

        var obtainTypeComboBox = new ComboBox<Stock.ObtainType>(i18n.get("book.column.obtain.type"));
        obtainTypeComboBox.setTextInputAllowed(false);
        obtainTypeComboBox.setEmptySelectionAllowed(false);
        obtainTypeComboBox.setItems(Stock.ObtainType.values());
        obtainTypeComboBox.setValue(book.getObtainType());
        bookBinder.forField(obtainTypeComboBox).bind(Stock::getObtainType, Stock::setObtainType);
        form.addComponent(obtainTypeComboBox);

        var obtainDateField = new DateField(i18n.get("book.column.obtain.date"));
        obtainDateField.setValue(book.getObtainDateAsLocalDate());
        bookBinder.forField(obtainDateField).bind(Stock::getObtainDateAsLocalDate, Stock::setObtainDate);
        form.addComponent(obtainDateField);

        var completeDateField = new DateField(i18n.get("book.column.progress.date.completed"));
        completeDateField.setRangeEnd(LocalDate.now());
        completeDateField.setValue(book.getCompletedDateAsLocalDate());
        bookBinder.forField(completeDateField).bind(Stock::getCompletedDateAsLocalDate, Stock::setCompletedDate);
        form.addComponent(completeDateField);

        var mediaTypeComboBox = new ComboBox<Stock.MediaType>(i18n.get("book.column.media.type"));
        mediaTypeComboBox.setTextInputAllowed(false);
        mediaTypeComboBox.setEmptySelectionAllowed(false);
        mediaTypeComboBox.setItems(Stock.MediaType.values());
        mediaTypeComboBox.setValue(book.getMediaType());
        bookBinder.forField(mediaTypeComboBox).bind(Stock::getMediaType, Stock::setMediaType);
        form.addComponent(mediaTypeComboBox);

        var boughtPlaceField = new TextField(i18n.get("book.column.bought.place"), book.getBoughtPlace());
        boughtPlaceField.setValue(book.getBoughtPlace());
        bookBinder.forField(boughtPlaceField)
                .withValidator(new StringLengthValidator(i18n.get("common.validator.text.length.over"), 0, 128))
                .bind(Stock::getBoughtPlace, Stock::setBoughtPlace);
        form.addComponent(boughtPlaceField);

        var cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(BookView.VIEW_NAME + "/" + book.getId()));

        var submitButton = new Button(i18n.get("edit-book.button.submit"), e -> {
            // Validate
            if (!bookBinder.writeBeanIfValid(book)) {
                Notifications.showWarning(i18n.get("common.notification.input.incomplete"));
                return;
            }

            // Submit
            try {
                book.setUpdatedTime(new Date());
                stockService.update(book);
            } catch (RuntimeException ex) {
                ErrorView.show(i18n.get("edit-book.error.update.failed"), ex);
                return;
            }

            // Next
            Notifications.showSuccess(i18n.get("edit-book.notification.update.success"));
            session.setAttribute(ATTR_BOOK_EDIT, null); // clear session attribute
            getUI().getNavigator().navigateTo(BooksView.VIEW_NAME);
        });
        submitButton.setIcon(VaadinIcons.CHECK);
        submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

        var buttonArea = new HorizontalLayout(cancelButton, submitButton);
        buttonArea.setSpacing(true);
        contentPane.addComponent(buttonArea);

        if (!userService.isLoggedIn()) {
            submitButton.setEnabled(false);
            contentPane.addComponent(new LoginRequiredLabel(i18n));
        }

        return contentPane;
    }
}
