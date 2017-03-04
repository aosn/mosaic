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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.Arrays;

/**
 * <p>Required session parameter:</p>
 * <ul>
 * <li>{@link #ATTR_STOCK_ADD} - A {@link io.github.aosn.mosaic.domain.model.catalog.ReleasedBook} object</li>
 * </ul>
 *
 * @author mikan
 * @since 0.3
 */
@SpringView(name = AddToStockView.VIEW_NAME, ui = MainUI.class)
@Slf4j
public class AddToStockView extends CustomComponent implements View {

    public static final String VIEW_NAME = "add-to-stock";
    public static final String ATTR_STOCK_ADD = "mosaic.stock.add";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final StockService stockService;
    private final VaadinSession session;

    @Autowired
    public AddToStockView(I18N i18n, UserService userService, PollService pollService, StockService stockService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.stockService = stockService;
        session = VaadinSession.getCurrent();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        ReleasedBook releasedBook = (ReleasedBook) session.getAttribute(ATTR_STOCK_ADD);
        if (releasedBook == null) {
            ErrorView.show(i18n.get("common.error.parameter.missing"), null);
            return;
        }
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(),
                createAddStockLayout(releasedBook)));
    }

    private Layout createAddStockLayout(ReleasedBook releasedBook) {
        ContentPane contentPane = new ContentPane();

        FormLayout bookInfo = new FormLayout();
        bookInfo.setCaption("Book information");
        bookInfo.setMargin(false);

        if (!Strings.isNullOrEmpty(releasedBook.getThumbnailUrl())) {
            Image thumbnail = new Image(releasedBook.getTitle(), new ExternalResource(releasedBook.getThumbnailUrl()));
            thumbnail.setCaption("Cover");
            bookInfo.addComponent(thumbnail);
        }

        TextField titleField = new TextField("Title");
        titleField.setValue(releasedBook.getTitle());
        bookInfo.addComponent(titleField);

        TextField publishedDate = new TextField("Published Date");
        publishedDate.setValue(releasedBook.getPublishedDate());
        bookInfo.addComponent(publishedDate);

        ComboBox visibilitySelect = new ComboBox("Visibility");
        visibilitySelect.setTextInputAllowed(false);
        visibilitySelect.setNullSelectionAllowed(false);
        visibilitySelect.addItems(Arrays.asList(Stock.Visibility.values()));
        visibilitySelect.setValue(Stock.Visibility.PUBLIC);
        bookInfo.addComponent(visibilitySelect);

        contentPane.addComponent(bookInfo);

        Button cancelButton = new Button("Cancel", e -> getUI().getNavigator().navigateTo(FindBookView.VIEW_NAME));

        Button submitButton = new Button("Add to stock", e -> {
            // Validator check
            Stock stock = Stock.builder()
                    .user(userService.getUser())
                    .isbn(releasedBook.getIsbn())
                    .visibility((Stock.Visibility) visibilitySelect.getValue())
                    .build();
            // submit to stock service
            session.setAttribute(ATTR_STOCK_ADD, null); // clear session attribute
            getUI().getNavigator().navigateTo(StockView.VIEW_NAME);
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
