package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.ComboBox;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.ui.view.style.Style;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link Group} selectable {@link ComboBox}.
 *
 * @author mikan
 * @since 0.2
 */
public class GroupComboBox extends ComboBox {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private final List<GroupEntry> entries;

    public GroupComboBox(String caption, List<Group> groups, int selectIndex) {
        super(caption);
        entries = groups.stream().map(GroupEntry::new).collect(Collectors.toList());
        setContainerDataSource(new BeanItemContainer<>(GroupEntry.class, entries));
        setNullSelectionAllowed(false);
        setTextInputAllowed(false);
        setItemCaptionPropertyId("caption");
        setItemIconPropertyId("icon");
        setStyleName(Style.GROUP_SELECT.className());
        select(entries.get(selectIndex));
    }

    public int getSelectIndex() {
        GroupEntry entry = (GroupEntry) getValue();
        return entries.indexOf(entry);
    }

    @SuppressWarnings("WeakerAccess") // BeanItemContainer requires public class
    @Getter
    public static class GroupEntry implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
        private String caption;
        private Resource icon;

        public GroupEntry(Group group) {
            caption = group.getOrganization() + "/" + group.getRepository();
            icon = new ExternalResource(User.Source.GITHUB.getIconUrl(group.getOrganization()));
        }
    }
}
