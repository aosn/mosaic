/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import org.vaadin.spring.i18n.I18N;

import java.util.List;

/**
 * @author mikan
 * @since 0.2
 */
public class BulkSelector extends HorizontalLayout {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public BulkSelector(I18N i18n, List<IssueTable.Row> rows) {
        setSpacing(true);

        // Select all
        Button selectAllButton = new Button(i18n.get("common.button.bulk.select.all"),
                e -> rows.forEach(r -> r.getCheckBox().setValue(true)));
        selectAllButton.setIcon(VaadinIcons.CHECK_SQUARE_O);
        selectAllButton.setStyleName(ValoTheme.BUTTON_TINY);
        addComponent(selectAllButton);

        // Select all for each reading parts
        rows.stream().map(r -> r.getCategory().getValue()).distinct().sorted().forEach(p -> {
            Button partButton = new Button(String.format(i18n.get("common.button.bulk.select.part"), p),
                    e -> rows.forEach(r -> r.getCheckBox().setValue(r.getCategory().getValue().contains(p))));
            partButton.setCaptionAsHtml(true);
            partButton.setIcon(VaadinIcons.CHECK_SQUARE_O);
            partButton.setStyleName(ValoTheme.BUTTON_TINY);
            addComponent(partButton);
        });

        // Deselect all
        Button deselectAllButton = new Button(i18n.get("common.button.bulk.deselect.all"),
                e -> rows.forEach(r -> r.getCheckBox().setValue(false)));
        deselectAllButton.setIcon(VaadinIcons.THIN_SQUARE);
        deselectAllButton.setStyleName(ValoTheme.BUTTON_TINY);
        addComponent(deselectAllButton);
    }
}
