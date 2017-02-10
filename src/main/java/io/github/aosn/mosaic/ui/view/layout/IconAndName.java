/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.ui.view.style.Style;

/**
 * @author mikan
 * @since 0.2
 */
public class IconAndName extends HorizontalLayout {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public IconAndName(User user) {
        super(new UserIcon(user), new Label(user.getName()));
        setSpacing(true);
        setStyleName(Style.ICON_AND_NAME.className());
    }

    private static class UserIcon extends Image {
        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

        private UserIcon(User user) {
            super();
            setSource(new ExternalResource(user.getIconUrl()));
            setStyleName(Style.USER_ICON.className());
            addClickListener(l -> UI.getCurrent().getPage().setLocation(user.getProfileUrl()));
        }
    }
}
