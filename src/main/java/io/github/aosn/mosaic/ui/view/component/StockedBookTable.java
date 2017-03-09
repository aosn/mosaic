/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.ui.view.BookView;
import lombok.Builder;
import lombok.Getter;
import org.vaadin.spring.i18n.I18N;

import java.io.Serializable;
import java.util.List;

/**
 * @author mikan
 * @since 0.3
 */
public class StockedBookTable extends Table {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public StockedBookTable(List<Row> rows, I18N i18n) {
        super(null, new BeanItemContainer<>(Row.class, rows));
        setPageLength(0);
        setColumnHeader("title", i18n.get("book.column.title"));
        setColumnHeader("progress", i18n.get("book.column.progress"));
        setVisibleColumns("title", "progress");
    }

    /**
     * Table row for {@link Stock} entity.
     *
     * @author mikan
     * @see com.vaadin.data.util.BeanItemContainer
     * @since 0.3
     */
    @Getter
    @Builder
    public static class Row implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
        private transient final Stock entity;
        private final Label title;
        private final Label progress;

        public static Row from(Stock entity, I18N i18n) {
            return builder()
                    .title(new Label(createBookLink(entity), ContentMode.HTML))
                    .progress(new ProgressLabel(entity, i18n))
                    .build();
        }

        private static String createBookLink(Stock stock) {
            return createAnchor("/#!" + BookView.VIEW_NAME + "/" + stock.getId(), stock.getTitle());
        }

        private static String createAnchor(String url, String text) {
            return "<a href=\"" + url + "\">" + text + "</a>";
        }
    }
}
