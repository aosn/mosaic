/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.google.common.base.Strings;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import io.github.aosn.mosaic.ui.view.AddToStockView;
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
        setColumnHeader("selectButton", "Sel.");
        setColumnHeader("title", "Title");
        setColumnHeader("publishedDate", "Published");
        setColumnHeader("thumbnail", "Cover");
        setVisibleColumns((Object[]) columnGroup.columns);
    }

    public void setDataSource(List<ReleasedBook> source) {
        List<Row> rows = source.stream()
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
                Notifications.showWarning("No books found.");
                break;
            case 1:
                Notifications.showSuccess("Book found.");
                break;
            default:
                Notifications.showSuccess(source.size() + " books found.");
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
     * @see com.vaadin.data.util.BeanItemContainer
     * @since 0.1
     */
    @Getter
    @Builder
    public static class Row implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
        private transient ReleasedBook entity;
        private Button selectButton;
        private String title;
        private String publishedDate;
        private Image thumbnail;

        public static Row from(ReleasedBook entity) {
            Button selectButton = new Button();
            selectButton.setIcon(FontAwesome.CHECK);
            selectButton.addClickListener(e -> {
                VaadinSession.getCurrent().setAttribute(AddToStockView.ATTR_STOCK_ADD, entity);
                UI.getCurrent().getNavigator().navigateTo(AddToStockView.VIEW_NAME);
            });
            Image thumbnail = null;
            if (!Strings.isNullOrEmpty(entity.getThumbnailUrl())) {
                thumbnail = new Image(entity.getTitle(), new ExternalResource(entity.getThumbnailUrl()));
                thumbnail.setWidth(70, Unit.PIXELS);
            }
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
