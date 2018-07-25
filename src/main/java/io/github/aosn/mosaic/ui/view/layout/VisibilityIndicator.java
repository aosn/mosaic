/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import org.vaadin.spring.i18n.I18N;

/**
 * @author mikan
 * @since 0.3
 */
public class VisibilityIndicator extends HorizontalLayout {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public VisibilityIndicator(Stock.Visibility visibility, I18N i18n) {
        var icon = new Label();
        var caption = new Label();
        switch (visibility) {
            case PUBLIC:
                caption.setValue(i18n.get("book.column.visibility.public"));
                icon.setIcon(VaadinIcons.GLOBE);
                break;
            case ALL_USER:
                caption.setValue(i18n.get("book.column.visibility.internal"));
                icon.setIcon(VaadinIcons.UNLOCK);
                break;
            case PRIVATE:
                caption.setValue(i18n.get("book.column.visibility.private"));
                icon.setIcon(VaadinIcons.LOCK);
                break;
            default: // currently unsupported
                caption.setValue(i18n.get("book.column.visibility.custom"));
                icon.setIcon(VaadinIcons.COG);
                break;
        }
        addComponents(icon, caption);
        setSpacing(true);
        setMargin(false);
    }
}
