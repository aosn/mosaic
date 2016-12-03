/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.ui.view.style.Style;
import org.vaadin.spring.i18n.I18N;

import java.time.LocalDate;

/**
 * @author mikan
 * @since 0.1
 */
class Footer extends VerticalLayout {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    Footer(I18N i18n) {
        String copyright = i18n.get("footer.copyright").replace("%d", String.valueOf(LocalDate.now().getYear()))
                .replace("%s", "<a href=\"" + i18n.get("footer.organization.url") +
                        "\" style=\"text-decoration:none;\">" + i18n.get("footer.organization") + "</a>");
        Label copyrightLabel = new Label(copyright, ContentMode.HTML);
        copyrightLabel.setStyleName(Style.COPYRIGHT.className());
        HorizontalLayout labelArea = new HorizontalLayout(copyrightLabel);
        addComponent(labelArea);
        setComponentAlignment(labelArea, Alignment.BOTTOM_CENTER);
    }
}
