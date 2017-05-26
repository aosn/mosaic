/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.ui.view.style.Style;

/**
 * @author mikan
 * @since 0.1
 */
public class ContentPane extends VerticalLayout {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public ContentPane() {
        setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        setSpacing(true);
        setMargin(false);
        setStyleName(Style.CONTENT_PANE.className());
    }
}
