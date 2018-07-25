/*
 * Copyright (C) 2016-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.ComboBox;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.ui.view.style.Style;

import java.util.List;

/**
 * A {@link Group} selectable {@link ComboBox}.
 *
 * @author mikan
 * @since 0.2
 */
public class GroupComboBox extends ComboBox<Group> {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private final List<Group> entries;

    public GroupComboBox(String caption, List<Group> groups, int selectIndex) {
        super(caption);
        entries = groups;
        setItems(entries);
        setEmptySelectionAllowed(false);
        setTextInputAllowed(false);
        setItemIconGenerator(i -> new ExternalResource(User.Source.GITHUB.getIconUrl(i.getOrganization())));
        setItemCaptionGenerator(i -> i.getOrganization() + "/" + i.getRepository());
        setStyleName(Style.GROUP_SELECT.className());
        setSelectedItem(entries.get(selectIndex));
    }

    public int getSelectIndex() {
        return entries.indexOf(getValue());
    }
}
