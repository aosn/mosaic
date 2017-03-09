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
    public static final String ATTR_BOOK_EDIT = "mosaic.book.edit";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final StockService stockService;
    private final VaadinSession session;

    @Autowired
    public EditBookView(I18N i18n, UserService userService, PollService pollService, StockService stockService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.stockService = stockService;
        session = VaadinSession.getCurrent();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Stock stock = (Stock) session.getAttribute(ATTR_BOOK_EDIT);
        if (stock == null) {
            ErrorView.show(i18n.get("common.error.parameter.missing"), null);
            return;
        }
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(),
                createEditStockLayout(stock)));
    }

    private Layout createEditStockLayout(Stock book) {
        ContentPane contentPane = new ContentPane();

        FormLayout form = new FormLayout();
        form.setCaption("Book information");
        form.setMargin(false);

        Image thumbnail = new Image(book.getTitle(), new ExternalResource(book.getThumbnailOrPlaceholder()));
        thumbnail.setCaption("Cover");
        form.addComponent(thumbnail);

        Label isbnLabel = new Label(book.getIsbn());
        isbnLabel.setCaption("ISBN");
        form.addComponent(isbnLabel);

        TextField titleField = new TextField("Title");
        titleField.setRequired(true);
        titleField.setValue(book.getTitle());
        titleField.setWidth(100, Unit.PERCENTAGE);
        titleField.addValidator(new StringLengthValidator("Text is too long", 0, 200, false));
        form.addComponent(titleField);

        TextField publishedDate = new TextField("Published Date");
        publishedDate.setRequired(true);
        publishedDate.setValue(book.getPublishedDate());
        publishedDate.addValidator(new AbstractStringValidator("Only allows yyyy-mm-dd, yyyy-mm or yyyy") {
            private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

            @Override
            protected boolean isValidValue(String value) {
                return Stock.isValidPublishedDate(value);
            }
        });
        form.addComponent(publishedDate);

        NumberField pagesField = new NumberField("Page count", book.getPageCount());
        pagesField.addValidator(v -> {
            if (pagesField.getValueAsInt() < 0) {
                throw new Validator.InvalidValueException("Out of range");
            }
        });
        form.addComponent(pagesField);

        TextField commentField = new TextField("Comment", book.getShortText());
        commentField.setWidth(100, Unit.PERCENTAGE);
        form.addComponent(commentField);

        TextArea bookReviewTextArea = new TextArea("Book review", book.getLongText());
        bookReviewTextArea.setWidth(100, Unit.PERCENTAGE);
        bookReviewTextArea.setDescription("Markdown allowed");
        form.addComponent(bookReviewTextArea);

        ComboBox visibilityComboBox = new ComboBox("Visibility");
        visibilityComboBox.setTextInputAllowed(false);
        visibilityComboBox.setNullSelectionAllowed(false);
        visibilityComboBox.addItems(Arrays.asList(Stock.Visibility.values()));
        visibilityComboBox.setValue(book.getVisibility());
        form.addComponent(visibilityComboBox);

        ComboBox progressComboBox = new ComboBox("Progress");
        progressComboBox.setTextInputAllowed(false);
        progressComboBox.setNullSelectionAllowed(false);
        progressComboBox.addItems(Arrays.asList(Stock.Progress.values()));
        progressComboBox.setValue(book.getProgress());
        form.addComponent(progressComboBox);

        ComboBox obtainTypeComboBox = new ComboBox("Obtain type");
        obtainTypeComboBox.setTextInputAllowed(false);
        obtainTypeComboBox.setNullSelectionAllowed(false);
        obtainTypeComboBox.addItems(Arrays.asList(Stock.ObtainType.values()));
        obtainTypeComboBox.setValue(book.getObtainType());
        form.addComponent(obtainTypeComboBox);

        DateField obtainDateField = new DateField("Obtain date", book.getObtainDate());
        form.addComponent(obtainDateField);

        DateField completeDateField = new DateField("Complete date", book.getCompletedDate());
        completeDateField.setRangeEnd(new Date());
        form.addComponent(completeDateField);

        ComboBox mediaTypeComboBox = new ComboBox("Media type");
        mediaTypeComboBox.setTextInputAllowed(false);
        mediaTypeComboBox.setNullSelectionAllowed(false);
        mediaTypeComboBox.addItems(Arrays.asList(Stock.MediaType.values()));
        mediaTypeComboBox.setValue(book.getMediaType());
        form.addComponent(mediaTypeComboBox);

        TextField boughtPlaceField = new TextField("Bought place", book.getBoughtPlace());
        boughtPlaceField.addValidator(new StringLengthValidator("Text is too long", 0, 128, true));
        form.addComponent(boughtPlaceField);

        contentPane.addComponent(form);

        Button cancelButton = new Button("Cancel", e -> getUI().getNavigator().navigateTo(FindBookView.VIEW_NAME));

        Button submitButton = new Button("Update book", e -> {
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
                book.setVisibility((Stock.Visibility) visibilityComboBox.getValue());
                book.setProgressPercentage(((Stock.Progress) progressComboBox.getValue()).actualValue());
                book.setTitle(titleField.getValue());
                book.setPublishedDate(publishedDate.getValue());
                book.setPageCount(pagesField.getValueAsInt());
                book.setShortText(commentField.getValue());
                book.setLongText(bookReviewTextArea.getValue());
                book.setObtainType((Stock.ObtainType) obtainTypeComboBox.getValue());
                book.setObtainDate(obtainDateField.getValue());
                book.setCompletedDate(completeDateField.getValue());
                book.setMediaType((Stock.MediaType) mediaTypeComboBox.getValue());
                book.setBoughtPlace(boughtPlaceField.getValue());
                book.setUpdatedTime(now);
                stockService.update(book);
            } catch (RuntimeException ex) {
                ErrorView.show("Failed to update book.", ex);
                return;
            }

            // Next
            Notifications.showSuccess("Book updated");
            session.setAttribute(ATTR_BOOK_EDIT, null); // clear session attribute
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
