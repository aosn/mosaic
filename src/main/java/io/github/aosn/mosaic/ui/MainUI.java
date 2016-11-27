/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.UI;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.config.SecurityConfig;
import io.github.aosn.mosaic.ui.view.ErrorView;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main UI.
 *
 * @author mikan
 * @since 0.1
 */
@Theme("valo")
@SpringUI(path = MainUI.PATH)
@StyleSheet(value = "vaadin:/" + SecurityConfig.CSS_PATH)
public class MainUI extends UI {

    public static final String PATH = "/";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private final SpringViewProvider viewProvider;

    @Autowired
    public MainUI(SpringViewProvider viewProvider) {
        this.viewProvider = viewProvider;
    }

    @Override
    protected void init(VaadinRequest request) {
        Navigator navigator = new Navigator(this, this);
        navigator.addProvider(viewProvider);
        navigator.setErrorView(ErrorView.class);
        setNavigator(navigator);
    }
}
