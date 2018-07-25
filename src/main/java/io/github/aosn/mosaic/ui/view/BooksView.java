/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.domain.service.stock.StockService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.StockedBookTable;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.spring.i18n.I18N;

import java.util.stream.Collectors;

/**
 * My books view.
 *
 * @author mikan
 * @since 0.3
 */
@SpringView(name = BooksView.VIEW_NAME, ui = MainUI.class)
@Slf4j
public class BooksView extends CustomComponent implements View {

    public static final String VIEW_NAME = "books";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final StockService stockService;

    public BooksView(I18N i18n, UserService userService, PollService pollService, StockService stockService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.stockService = stockService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(), createMyStockLayout()));
    }

    private Layout createMyStockLayout() {
        var contentPane = new ContentPane();

        var stocks = stockService.getAll(userService.getUser());

        var titleLabel = new Label(i18n.get("books.label.title"));
        contentPane.addComponent(titleLabel);

        var addBookButton = new Button(i18n.get("books.button.add"),
                e -> getUI().getNavigator().navigateTo(FindBookView.VIEW_NAME));
        addBookButton.setIcon(VaadinIcons.PLUS);
        addBookButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
        contentPane.addComponent(addBookButton);
        contentPane.setComponentAlignment(addBookButton, Alignment.MIDDLE_RIGHT);

        if (stocks.isEmpty()) {
            var label = new Label(VaadinIcons.INFO_CIRCLE.getHtml() + " " +
                    i18n.get("books.label.empty"), ContentMode.HTML);
            contentPane.addComponent(label);
            contentPane.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        } else {
            var rows = stocks.stream()
                    .map(s -> StockedBookTable.Row.from(s, i18n)).collect(Collectors.toList());
            var table = new StockedBookTable(rows, i18n);
            contentPane.addComponent(table);
        }

        return contentPane;
    }
}
