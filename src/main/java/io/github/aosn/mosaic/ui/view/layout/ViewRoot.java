package io.github.aosn.mosaic.ui.view.layout;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import io.github.aosn.mosaic.MosaicApplication;

/**
 * @author mikan
 * @since 0.1
 */
public class ViewRoot extends VerticalLayout {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public ViewRoot(Header header, Component... components) {
        setSizeFull();
        setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        setStyleName(Reindeer.LAYOUT_BLUE);
        addComponent(header);
        addComponents(components);
    }
}
