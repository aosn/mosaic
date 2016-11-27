/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Label;
import io.github.aosn.mosaic.MosaicApplication;
import org.vaadin.spring.i18n.I18N;


/**
 * @author mikan
 * @since 0.1
 */
public class LoginRequiredLabel extends Label {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public LoginRequiredLabel(I18N i18n) {
        setValue(i18n.get("common.caption.login.required"));
        setIcon(FontAwesome.WARNING);
        setCaption(i18n.get("common.caption.warning"));
    }
}
