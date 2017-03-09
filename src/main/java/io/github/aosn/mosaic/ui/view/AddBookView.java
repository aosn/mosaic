/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
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
import io.github.aosn.mosaic.ui.view.component.NumberField;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Notifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.Arrays;
import java.util.Date;

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
                createAddStockLayout(releasedBook)));
    }

    private Layout createAddStockLayout(ReleasedBook book) {
        ContentPane contentPane = new ContentPane();

        FormLayout form = new FormLayout();
        form.setCaption(i18n.get("add-book.label.title"));
        form.setMargin(false);

        Image thumbnail = new Image(book.getTitle(), new ExternalResource(book.getThumbnailOrPlaceholder()));
        thumbnail.setCaption(i18n.get("book.column.cover"));
        form.addComponent(thumbnail);

        Label isbnLabel = new Label(book.getIsbn());
        isbnLabel.setCaption(i18n.get("book.column.isbn"));
        form.addComponent(isbnLabel);

        TextField titleField = new TextField(i18n.get("book.column.title"));
        titleField.setRequired(true);
        titleField.setValue(book.getTitle());
        titleField.setWidth(100, Unit.PERCENTAGE);
        titleField.addValidator(
                new StringLengthValidator(i18n.get("common.validator.text.length.over"), 0, 200, false));
        form.addComponent(titleField);

        TextField publishedDate = new TextField(i18n.get("book.column.published.date"));
        publishedDate.setRequired(true);
        publishedDate.setValue(book.getPublishedDate());
        publishedDate.addValidator(new AbstractStringValidator(i18n.get("edit-book.validator.published.date")) {
            private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

            @Override
            protected boolean isValidValue(String value) {
                return Stock.isValidPublishedDate(value);
            }
        });
        form.addComponent(publishedDate);

        NumberField pagesField = new NumberField(i18n.get("book.column.page.count"), book.getPageCount());
        pagesField.addValidator(v -> {
            if (pagesField.getValueAsInt() < 0) {
                throw new Validator.InvalidValueException(i18n.get("common.validator.date.range.over"));
            }
        });
        form.addComponent(pagesField);

        TextField commentField = new TextField(i18n.get("book.column.text.short"));
        commentField.setWidth(100, Unit.PERCENTAGE);
        form.addComponent(commentField);

        TextArea bookReviewTextArea = new TextArea(i18n.get("book.column.text.long"));
        bookReviewTextArea.setWidth(100, Unit.PERCENTAGE);
        bookReviewTextArea.setDescription(i18n.get("edit-book.description.text.long"));
        form.addComponent(bookReviewTextArea);

        ComboBox visibilityComboBox = new ComboBox(i18n.get("book.column.visibility"));
        visibilityComboBox.setTextInputAllowed(false);
        visibilityComboBox.setNullSelectionAllowed(false);
        visibilityComboBox.addItems(Arrays.asList(Stock.Visibility.values()));
        visibilityComboBox.setValue(Stock.Visibility.PUBLIC);
        form.addComponent(visibilityComboBox);

        ComboBox progressComboBox = new ComboBox(i18n.get("book.column.progress"));
        progressComboBox.setTextInputAllowed(false);
        progressComboBox.setNullSelectionAllowed(false);
        progressComboBox.addItems(Arrays.asList(Stock.Progress.values()));
        progressComboBox.setValue(Stock.Progress.NOT_STARTED);
        form.addComponent(progressComboBox);

        ComboBox obtainTypeComboBox = new ComboBox(i18n.get("book.column.obtain.type"));
        obtainTypeComboBox.setTextInputAllowed(false);
        obtainTypeComboBox.setNullSelectionAllowed(false);
        obtainTypeComboBox.addItems(Arrays.asList(Stock.ObtainType.values()));
        obtainTypeComboBox.setValue(Stock.ObtainType.BUY);
        form.addComponent(obtainTypeComboBox);

        DateField obtainDateField = new DateField(i18n.get("book.column.obtain.date"));
        form.addComponent(obtainDateField);

        DateField completeDateField = new DateField(i18n.get("book.column.progress.date.completed"));
        completeDateField.setRangeEnd(new Date());
        form.addComponent(completeDateField);

        ComboBox mediaTypeComboBox = new ComboBox(i18n.get("book.column.media.type"));
        mediaTypeComboBox.setTextInputAllowed(false);
        mediaTypeComboBox.setNullSelectionAllowed(false);
        mediaTypeComboBox.addItems(Arrays.asList(Stock.MediaType.values()));
        mediaTypeComboBox.setValue(book.isEBook() ? Stock.MediaType.KINDLE : Stock.MediaType.PAPER);
        form.addComponent(mediaTypeComboBox);

        TextField boughtPlaceField = new TextField(i18n.get("book.column.bought.place"));
        boughtPlaceField.addValidator(
                new StringLengthValidator(i18n.get("common.validator.text.length.over"), 0, 128, true));
        form.addComponent(boughtPlaceField);

        contentPane.addComponent(form);

        Button cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(FindBookView.VIEW_NAME));

        Button submitButton = new Button(i18n.get("add-book.button.submit"), e -> {
            // Validate
            if (!titleField.isValid() || !publishedDate.isValid() || !pagesField.isValid() || !commentField.isValid() ||
                    !bookReviewTextArea.isValid() || !visibilityComboBox.isValid() || !progressComboBox.isValid() ||
                    !obtainTypeComboBox.isValid() || !obtainDateField.isValid() || !completeDateField.isValid() ||
                    !mediaTypeComboBox.isValid()) {
                Notifications.showWarning(i18n.get("common.notification.input.incomplete"));
                return;
            }

            // Submit
            Date now = new Date();
            try {
                Stock stock = Stock.builder()
                        .user(userService.getUser())
                        .isbn(Stock.normalizeIsbn(book.getIsbn()))
                        .title(titleField.getValue())
                        .subtitle(book.getSubtitle())
                        .publishedDate(publishedDate.getValue())
                        .pageCount(pagesField.getValueAsInt())
                        .shortText(commentField.getValue())
                        .longText(bookReviewTextArea.getValue())
                        .visibility((Stock.Visibility) visibilityComboBox.getValue())
                        .progress(((Stock.Progress) progressComboBox.getValue()).actualValue())
                        .obtainType((Stock.ObtainType) obtainTypeComboBox.getValue())
                        .obtainDate(obtainDateField.getValue())
                        .completedDate(completeDateField.getValue())
                        .mediaType((Stock.MediaType) mediaTypeComboBox.getValue())
                        .boughtPlace(boughtPlaceField.getValue())
                        .thumbnailUrl(book.getThumbnailUrl())
                        .createdTime(now)
                        .updatedTime(now)
                        .build();
                stockService.add(stock);
            } catch (RuntimeException ex) {
                ErrorView.show(i18n.get("add-book.error.add.failed"), ex);
                return;
            }

            // Next
            Notifications.showSuccess(i18n.get("add-book.notification.add.success"));
            session.setAttribute(ATTR_BOOK_ADD, null); // clear session attribute
            getUI().getNavigator().navigateTo(BooksView.VIEW_NAME);
        });
        submitButton.setIcon(FontAwesome.CHECK);
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
