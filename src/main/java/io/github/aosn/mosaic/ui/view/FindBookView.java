/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.catalog.CatalogService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.ReleasedBookTable;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Notifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.List;

/**
 * Find book create the catalog for add to stock.
 *
 * @author mikan
 * @since 0.3
 */
@SpringView(name = FindBookView.VIEW_NAME, ui = MainUI.class)
@Slf4j
public class FindBookView extends CustomComponent implements View {

    static final String VIEW_NAME = "find-book";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final CatalogService catalogService;

    @Autowired
    public FindBookView(I18N i18n, UserService userService, PollService pollService, CatalogService catalogService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.catalogService = catalogService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(), createFindBookLayout()));
    }

    private Layout createFindBookLayout() {
        ContentPane contentPane = new ContentPane();

        FormLayout searchInputForm = new FormLayout();
        searchInputForm.setCaption(i18n.get("find-book.label.title"));
        searchInputForm.setMargin(false);
        contentPane.addComponent(searchInputForm);

        TextField searchField = new TextField(i18n.get("find-book.caption.search.input"));
        searchField.setWidth(100, Unit.PERCENTAGE);
        searchInputForm.addComponent(searchField);

        ReleasedBookTable searchResultTable = new ReleasedBookTable(i18n);
        searchResultTable.setVisible(false);

        Button searchButton = new Button(i18n.get("find-book.button.search"), e -> {
            if (Strings.isNullOrEmpty(searchField.getValue())) {
                Notifications.showWarning(i18n.get("common.notification.input.incomplete"));
                return;
            }
            List<ReleasedBook> searchResult;
            try {
                String isbn = Stock.normalizeIsbn(searchField.getValue());
                searchResult = catalogService.searchByIsbn(isbn);
            } catch (IllegalArgumentException ex) {
                // Not a ISBN, search by name
                searchResult = catalogService.searchByKeyword(searchField.getValue());
            } catch (NullPointerException ex) {
                Notifications.showWarning(i18n.get("common.notification.input.incomplete"));
                return;
            }
            if (searchResult.isEmpty()) {
                Notifications.showWarning(i18n.get("find-book.notification.no.match"));
                return;
            }
            searchResultTable.setDataSource(searchResult);
            searchResultTable.setVisible(true);
        });
        searchButton.setIcon(FontAwesome.SEARCH);
        searchButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        contentPane.addComponent(searchButton);

        // Enter to search
        searchField.addShortcutListener(new ShortcutListener(i18n.get("find-book.caption.search.enter"),
                ShortcutAction.KeyCode.ENTER, null) {
            private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

            @Override
            public void handleAction(Object sender, Object target) {
                searchButton.click();
            }
        });

        contentPane.addComponent(searchResultTable);

        return contentPane;
    }
}
