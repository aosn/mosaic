/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.UI;
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
@Theme("valo")
@SpringUI(path = ErrorUI.PATH)
@StyleSheet(value = "vaadin:/" + SecurityConfig.CSS_PATH)
public class ErrorUI extends UI {

    public static final String PATH = "/error";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private final SpringViewProvider viewProvider;
    private transient final I18N i18n;

    @Autowired
    public ErrorUI(SpringViewProvider viewProvider, I18N i18n) {
        this.viewProvider = viewProvider;
        this.i18n = i18n;
    }

    @Override
    protected void init(VaadinRequest request) {
        i18n.setRevertToDefaultBundle(true);
        getReconnectDialogConfiguration().setDialogText(i18n.get("system.caption.reconnect"));
        if (VaadinService.getCurrent() != null) {
            VaadinService.getCurrent().setSystemMessagesProvider(new I18nSystemMessageProvider(i18n));
        }
        Navigator navigator = new Navigator(this, this);
        navigator.addProvider(viewProvider);
        navigator.setErrorView(ErrorView.class);
        setNavigator(navigator);
        navigator.navigateTo(ErrorView.VIEW_NAME);
    }
}
