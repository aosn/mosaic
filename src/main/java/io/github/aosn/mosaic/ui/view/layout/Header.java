/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.server.ClassResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.config.SecurityConfig;
import io.github.aosn.mosaic.controller.UserController;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.style.Style;
import org.vaadin.spring.i18n.I18N;

/**
 * UI-shared header component.
 *
 * @author mikan
 * @since 0.1
 */
class Header extends VerticalLayout {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    Header(I18N i18n, UserService userService) {
        setSpacing(true);

        // Logo
        Image logo = new Image(null, new ClassResource("logo.png"));
        logo.addClickListener(e -> UI.getCurrent().getNavigator().navigateTo(MainUI.PATH));
        addComponent(logo);

        // Login Bar
        Label welcomeLabel = new Label();
        HorizontalLayout loginBar;

        if (userService.isLoggedIn()) {
            welcomeLabel.setValue(FontAwesome.USER.getHtml() + " " + userService.getName());
            welcomeLabel.setContentMode(ContentMode.HTML);
            String newIssueUrl = userService.getNewIssueUrl(); // UserService isn't serializable
            Button newIssueButton = new Button(i18n.get("header.button.propose"),
                    e -> getUI().getPage().setLocation(newIssueUrl));
            newIssueButton.setIcon(FontAwesome.LIGHTBULB_O);
            Button logoutButton = new Button(i18n.get("header.button.logout"),
                    e -> getUI().getPage().setLocation(UserController.LOGOUT_PATH));
            loginBar = new HorizontalLayout(welcomeLabel, newIssueButton, logoutButton);
        } else {
            welcomeLabel.setValue(i18n.get("header.label.login"));
            Button loginButton = new Button(i18n.get("header.button.login.github"),
                    e -> getUI().getPage().setLocation(SecurityConfig.LOGIN_PATH_GITHUB));
            loginButton.setIcon(FontAwesome.GITHUB);
            loginBar = new HorizontalLayout(welcomeLabel, loginButton);
        }
        loginBar.setSpacing(true);
        loginBar.setStyleName(Style.LOGIN_BAR.className());
        loginBar.setComponentAlignment(welcomeLabel, Alignment.MIDDLE_CENTER);
        addComponent(loginBar);
        setComponentAlignment(loginBar, Alignment.MIDDLE_RIGHT);
    }
}
