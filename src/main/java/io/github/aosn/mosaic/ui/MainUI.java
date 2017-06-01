/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui;

import com.vaadin.annotations.*;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.config.SecurityConfig;
import io.github.aosn.mosaic.ui.i18n.I18nSystemMessageProvider;
import io.github.aosn.mosaic.ui.view.ErrorView;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

/**
 * Main UI.
 *
 * @author mikan
 * @since 0.1
 */
@SpringUI(path = MainUI.PATH)
@SpringViewDisplay
@Theme(ValoTheme.THEME_NAME)
@Title(MosaicApplication.DEFAULT_TITLE)
@StyleSheet(value = "vaadin:/" + SecurityConfig.CSS_PATH)
@Viewport("user-scalable=no,width=500")
@Widgetset("com.vaadin.v7.Vaadin7WidgetSet")
public class MainUI extends UI {

    public static final String PATH = "/";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;

    @Autowired
    public MainUI(I18N i18n) {
        this.i18n = i18n;
    }

    @Override
    protected void init(VaadinRequest request) {
        i18n.setRevertToDefaultBundle(true);
        getReconnectDialogConfiguration().setDialogText(i18n.get("system.caption.reconnect"));
        if (VaadinService.getCurrent() != null) {
            VaadinService.getCurrent().setSystemMessagesProvider(new I18nSystemMessageProvider(i18n));
        }
        getNavigator().setErrorView(ErrorView.class);
    }
}
