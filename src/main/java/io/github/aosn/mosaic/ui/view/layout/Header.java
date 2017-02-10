/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.config.SecurityConfig;
import io.github.aosn.mosaic.controller.UserController;
import io.github.aosn.mosaic.domain.model.poll.Group;
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

    Header(I18N i18n, UserService userService, Group group) {
        setSpacing(true);

        // Logo
        Image logo = new Image(null, new ExternalResource("/VAADIN/img/logo.png"));
        logo.addClickListener(e -> UI.getCurrent().getPage().setLocation(MainUI.PATH));
        addComponent(logo);

        // Login Bar
        HorizontalLayout loginBar;
        if (userService.isLoggedIn()) {
            String newIssueUrl = userService.getNewIssueUrl(group); // UserService isn't serializable
            Button newIssueButton = new Button(i18n.get("header.button.propose"),
                    e -> getUI().getPage().setLocation(newIssueUrl));
            newIssueButton.setIcon(FontAwesome.LIGHTBULB_O);
            Button logoutButton = new Button(i18n.get("header.button.logout"),
                    e -> getUI().getPage().setLocation(UserController.LOGOUT_PATH));
            IconAndName iconAndName = new IconAndName(userService.getUser());
            loginBar = new HorizontalLayout(iconAndName, newIssueButton, logoutButton);
            loginBar.setComponentAlignment(iconAndName, Alignment.MIDDLE_CENTER);
        } else {
            Label welcomeLabel = new Label(i18n.get("header.label.login"));
            Button loginButton = new Button(i18n.get("header.button.login.github"),
                    e -> getUI().getPage().setLocation(SecurityConfig.LOGIN_PATH_GITHUB));
            loginButton.setIcon(FontAwesome.GITHUB);
            loginBar = new HorizontalLayout(welcomeLabel, loginButton);
            loginBar.setComponentAlignment(welcomeLabel, Alignment.MIDDLE_CENTER);
        }
        loginBar.setSpacing(true);
        loginBar.setStyleName(Style.LOGIN_BAR.className());
        addComponent(loginBar);
        setComponentAlignment(loginBar, Alignment.MIDDLE_RIGHT);
    }
}
