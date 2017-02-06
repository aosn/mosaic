/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.style;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;

/**
 * Wrapper for {@link Notification}.
 *
 * @author mikan
 * @since 0.2
 */
public class Notifications {

    private Notifications() {
    }

    public static void showNormal(String message) {
        Notification notification = new Notification(message, Notification.Type.TRAY_NOTIFICATION);
        notification.setPosition(Position.TOP_CENTER);
        notification.show(Page.getCurrent());
    }

    public static void showWarning(String message) {
        Notification.show(message, Notification.Type.WARNING_MESSAGE);
    }
}
