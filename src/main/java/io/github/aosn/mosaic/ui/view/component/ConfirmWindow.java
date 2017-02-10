/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import org.vaadin.spring.i18n.I18N;

/**
 * Provides a confirm window.
 * <p>Usage: {@code UI.getCurrent().addWindow(new ConfirmWindow(...);}</p>
 *
 * @author mikan
 * @since 0.2
 */
public class ConfirmWindow extends Window {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    /**
     * Constructs a confirm window.
     * <p>The dialog is automatically closed when OK or Cancel is pressed.</p>
     *
     * @param message  message
     * @param i18n     message source
     * @param okAction invoked when OK button is pressed
     */
    public ConfirmWindow(String message, I18N i18n, Button.ClickListener okAction) {
        super();
        center();
        setModal(true);
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.addComponent(new Label(message, ContentMode.HTML));
        Button okButton = new Button(i18n.get("common.button.ok"), e -> {
            try {
                okAction.buttonClick(e);
            } finally {
                close();
            }
        });
        okButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        HorizontalLayout buttonArea = new HorizontalLayout(
                okButton,
                new Button(i18n.get("common.button.cancel"), e -> close()));
        buttonArea.setSpacing(true);
        content.addComponent(buttonArea);
        content.setComponentAlignment(buttonArea, Alignment.BOTTOM_CENTER);
        Panel panel = new Panel(i18n.get("common.caption.confirm"), content);
        panel.setHeight(300, Unit.PIXELS);
        panel.setWidth(400, Unit.PIXELS);
        setContent(panel);
    }
}
