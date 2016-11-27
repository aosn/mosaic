/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.ui.Label;
import io.github.aosn.mosaic.MosaicApplication;

/**
 * @author mikan
 * @since 0.1
 */
public class HeadingLabel extends Label {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public HeadingLabel(String title) {
        setValue(title);
        setStyleName("heading");
    }
}
