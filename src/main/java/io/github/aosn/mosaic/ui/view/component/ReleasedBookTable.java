/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.UI;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.Table;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import io.github.aosn.mosaic.ui.view.AddBookView;
import io.github.aosn.mosaic.ui.view.style.Notifications;
import lombok.Builder;
import lombok.Getter;
import org.vaadin.spring.i18n.I18N;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mikan
 * @since 0.3
 */
public class ReleasedBookTable extends Table {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private final ColumnGroup columnGroup;

    public ReleasedBookTable(I18N i18n) {
        super(null, new BeanItemContainer<>(Row.class));
        this.i18n = i18n;
        this.columnGroup = ColumnGroup.DEFAULT;
        setPageLength(0);
        setColumnHeader("selectButton", i18n.get("new.column.select"));
        setColumnHeader("title", i18n.get("book.column.title"));
        setColumnHeader("publishedDate", i18n.get("book.column.published.date"));
        setColumnHeader("thumbnail", i18n.get("book.column.cover"));
        setVisibleColumns((Object[]) columnGroup.columns);
    }

    public void setDataSource(List<ReleasedBook> source) {
        var rows = source.stream()
                .map(Row::from)
                .sorted((Comparator.comparing(Row::getPublishedDate).reversed()))
                .collect(Collectors.toList());
        setContainerDataSource(new BeanItemContainer<>(Row.class, rows));
        setSortContainerPropertyId("publishedDate");
        setSortAscending(false);
        setVisibleColumns((Object[]) columnGroup.columns);
        refreshRenderedCells();
        switch (source.size()) {
            case 0:
                Notifications.showWarning(i18n.get("find-book.notification.found.0"));
                break;
            case 1:
                Notifications.showSuccess(i18n.get("find-book.notification.found.1"));
                break;
            default:
                Notifications.showSuccess(String.format(i18n.get("find-book.notification.found.n"), source.size()));
                break;
        }
    }

    public enum ColumnGroup {
        DEFAULT("selectButton", "thumbnail", "title", "publishedDate");
        private final String[] columns;

        ColumnGroup(String... columns) {
            this.columns = columns;
        }
    }

    /**
     * Table row for {@link ReleasedBook} entity.
     *
     * @author mikan
     * @since 0.3
     */
    @Getter
    @Builder
    public static class Row implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
        private transient final ReleasedBook entity;
        private final Button selectButton;
        private final String title;
        private final String publishedDate;
        private final Image thumbnail;

        public static Row from(ReleasedBook entity) {
            var selectButton = new Button();
            selectButton.setIcon(VaadinIcons.CHEVRON_RIGHT);
            selectButton.addClickListener(e ->
                    UI.getCurrent().getNavigator().navigateTo(AddBookView.VIEW_NAME + "/" + entity.getIsbn()));
            var thumbnail = new Image(entity.getTitle(), new ExternalResource(entity.getThumbnailOrPlaceholder()));
            thumbnail.setWidth(70, Unit.PIXELS);
            return Row.builder()
                    .entity(entity)
                    .selectButton(selectButton)
                    .title(entity.getTitle())
                    .publishedDate(entity.getPublishedDate())
                    .thumbnail(thumbnail)
                    .build();
        }
    }
}
