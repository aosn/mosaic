/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import org.vaadin.spring.i18n.I18N;

/**
 * Root layout for general views.
 *
 * @author mikan
 * @since 0.1
 */
public class ViewRoot extends VerticalLayout {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public ViewRoot(I18N i18n, UserService userService, Group group, Component... components) {
        setSizeFull();
        setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        setStyleName(Reindeer.LAYOUT_BLUE);
        addComponent(new Header(i18n, userService, group));
        addComponents(components);
        addComponent(new Footer(i18n));
    }
}
