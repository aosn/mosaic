/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.Header;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

/**
 * Error page.
 *
 * @author mikan
 * @since 0.1
 */
@Slf4j
@SpringView(name = ErrorView.VIEW_NAME)
public class ErrorView extends CustomComponent implements View {

    public static final String VIEW_NAME = "error";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private static final String ATTR_ERROR_MESSAGE = "mosaic.ui.error.message";
    private static final String ATTR_ERROR_THROWABLE = "mosaic.ui.error.throwable";
    private transient final I18N i18n;
    private transient final UserService userService;

    @Autowired
    public ErrorView(I18N i18n, UserService userService) {
        this.i18n = i18n;
        this.userService = userService;
    }

    static void show(String message, Throwable throwable) {
        UI ui = UI.getCurrent();
        VaadinSession session = ui.getSession();
        session.setAttribute(ATTR_ERROR_MESSAGE, message);
        session.setAttribute(ATTR_ERROR_THROWABLE, throwable);
        ui.getNavigator().navigateTo(VIEW_NAME);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setCompositionRoot(new ViewRoot(new Header(i18n, userService), createErrorLayout()));
    }

    private Layout createErrorLayout() {
        ContentPane contentPane = new ContentPane();

        VaadinSession session = VaadinSession.getCurrent();
        String message = (String) session.getAttribute(ATTR_ERROR_MESSAGE);
        Throwable throwable = (Throwable) session.getAttribute(ATTR_ERROR_THROWABLE);

        Label errorLabel = new Label("ERROR");
        errorLabel.setStyleName("error-label");
        contentPane.addComponent(errorLabel);

        if (message != null) {
            contentPane.addComponent(new Label(message));
        }
        if (throwable != null) {
            contentPane.addComponent(new Label(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()));
            log.error("Received error: " + throwable.getMessage(), throwable);
        }

        Button backButton = new Button("Back", e -> getUI().getPage().setLocation(MainUI.PATH));
        contentPane.addComponent(backButton);
        contentPane.setComponentAlignment(backButton, Alignment.MIDDLE_CENTER);

        return contentPane;
    }
}
