/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.google.common.base.Strings;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.domain.service.stock.StockService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.ProgressLabel;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.layout.VisibilityIndicator;
import lombok.extern.slf4j.Slf4j;
import org.pegdown.PegDownProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/**
 * A {@link View} for single {@link Stock}.
 * <p>
 * <p>Required path parameter:</p>
 * <ul>
 * <li>{@code /:stock_id} - A stock id as {@link Long}</li>
 * </ul>
 *
 * @author mikan
 * @since 0.3
 */
@SpringView(name = BookView.VIEW_NAME, ui = MainUI.class)
@Slf4j
public class BookView extends CustomComponent implements View {

    public static final String VIEW_NAME = "book";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/mm/dd");
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final StockService stockService;

    @Autowired
    public BookView(I18N i18n, UserService userService, PollService pollService, StockService stockService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.stockService = stockService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // Parse parameter
        if (Strings.isNullOrEmpty(event.getParameters())) {
            ErrorView.show(i18n.get("common.error.parameter.missing"), null);
            return;
        }
        long stockId;
        try {
            stockId = Stream.of(event.getParameters().split("/"))
                    .mapToLong(Long::parseLong)
                    .findFirst().orElseThrow(NoSuchElementException::new);
        } catch (RuntimeException e) {
            ErrorView.show(i18n.get("common.error.parameter.missing"), e);
            return;
        }
        // Lookup
        Stock stock = stockService.get(stockId);
        if (stock == null) {
            ErrorView.show("No such book: " + stockId, null);
            return;
        }
        // Access level
        if (!stock.isAccessible(userService.getUser())) {
            ErrorView.show("Permission denied.", null);
            return;
        }
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(), createBookLayout(stock)));
    }

    private Layout createBookLayout(Stock book) {
        ContentPane contentPane = new ContentPane();

        VisibilityIndicator visibility = new VisibilityIndicator(book.getVisibility(), i18n);
        contentPane.addComponent(visibility);
        contentPane.setComponentAlignment(visibility, Alignment.MIDDLE_RIGHT);

        Image cover = new Image("", new ExternalResource(book.getThumbnailOrPlaceholder()));
        contentPane.addComponent(cover);

        FormLayout form = new FormLayout();
        form.setMargin(false);
        contentPane.addComponent(form);

        Label title = new Label(book.getTitle());
        title.setCaption("Title");
        form.addComponent(title);

        Label isbn = new Label(book.getIsbn());
        isbn.setCaption("isbn");
        form.addComponent(isbn);

        Label pageCount = new Label(String.valueOf(book.getPageCount()));
        pageCount.setCaption("Page count");
        form.addComponent(pageCount);

        HorizontalLayout progress = new HorizontalLayout(new ProgressLabel(book, i18n));
        progress.setCaption("Progress");
        form.addComponent(progress);

        if (book.getProgress() == Stock.Progress.COMPLETED && book.getCompletedDate() != null) {
            Label completedDate = new Label(DATE_FORMAT.format(book.getCompletedDate()));
            completedDate.setCaption("Completed date");
            form.addComponent(completedDate);
        }

        Label obtainType = new Label(book.getObtainType().name());
        obtainType.setCaption("Obtain type");
        form.addComponent(obtainType);

        if (book.getObtainDate() != null) {
            Label obtainDate = new Label(DATE_FORMAT.format(book.getObtainDate()));
            obtainDate.setCaption("Obtain date");
            form.addComponent(obtainDate);
        }

        if (!Strings.isNullOrEmpty(book.getBoughtPlace())) {
            Label boughtPlace = new Label(book.getBoughtPlace());
            boughtPlace.setCaption("Bought place");
            form.addComponent(boughtPlace);
        }

        Label comment = new Label(book.getShortText());
        comment.setCaption("Comment");
        form.addComponent(comment);

        String reviewContent = book.getLongText();
        Label review = new Label("", ContentMode.HTML);
        review.setCaption("Review");
        if (reviewContent.isEmpty()) {
            review.setValue("(No review)");
        } else {
            review.setValue(new PegDownProcessor().markdownToHtml(reviewContent));
        }
        form.addComponent(review);

        if (userService.isLoggedIn()) {
            Button editButton = new Button("Edit", e -> {
                VaadinSession.getCurrent().setAttribute(EditBookView.ATTR_BOOK_EDIT, book);
                getUI().getNavigator().navigateTo(EditBookView.VIEW_NAME);
            });
            editButton.setIcon(FontAwesome.EDIT);
            editButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
            contentPane.addComponent(editButton);
        }

        return contentPane;
    }
}
