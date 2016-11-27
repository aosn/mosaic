/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.config.SecurityConfig;
import io.github.aosn.mosaic.controller.UserController;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import org.vaadin.spring.i18n.I18N;

/**
 * UI-shared header component.
 *
 * @author mikan
 * @since 0.1
 */
public class Header extends VerticalLayout {
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public Header(I18N i18n, UserService userService) {
        setSpacing(true);

        // Title
        Label titleLabel = new Label(i18n.get("header.caption.title"));
        titleLabel.setStyleName("title");
        addComponent(titleLabel);

        // Subtitle
        Label subtitleLabel = new Label(i18n.get("header.caption.subtitle"));
        subtitleLabel.setStyleName("subtitle");
        addComponent(subtitleLabel);

        // Login Bar
        Label welcomeLabel = new Label();
        Button loginLogoutButton = new Button();

        if (userService.isLoggedIn()) {
            welcomeLabel.setValue(FontAwesome.USER.getHtml() + " " + userService.getName());
            welcomeLabel.setContentMode(ContentMode.HTML);
            loginLogoutButton.setCaption(i18n.get("header.button.logout"));
            loginLogoutButton.addClickListener(e -> getUI().getPage().setLocation(UserController.LOGOUT_PATH));
        } else {
            welcomeLabel.setValue(i18n.get("header.caption.login"));
            loginLogoutButton.setCaption(i18n.get("header.button.login.github"));
            loginLogoutButton.setIcon(FontAwesome.GITHUB);
            loginLogoutButton.addClickListener(e -> getUI().getPage().setLocation(SecurityConfig.LOGIN_PATH_GITHUB));
        }

        HorizontalLayout loginBar = new HorizontalLayout(welcomeLabel, loginLogoutButton);
        loginBar.setSpacing(true);
        loginBar.setStyleName("login-bar");
        loginBar.setComponentAlignment(welcomeLabel, Alignment.MIDDLE_CENTER);
        addComponent(loginBar);
        setComponentAlignment(loginBar, Alignment.MIDDLE_RIGHT);
    }
}
