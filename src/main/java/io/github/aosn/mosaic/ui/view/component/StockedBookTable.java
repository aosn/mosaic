/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.ui.view.style.Style;
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
        setColumnHeader("title", "Title");
        setColumnHeader("progress", "Progress");
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
        private final String title;
        private final Label progress;

        public static Row from(Stock entity, I18N i18n) {
            return builder()
                    .title(entity.getBookName())
                    .progress(createProgressLabel(entity.getProgress(), i18n))
                    .build();
        }

        private static Label createProgressLabel(Stock.Progress progress, I18N i18n) {
            Label label = new Label();
            switch (progress) {
                case NOT_STARTED:
                    label.setValue("NOT STARTED");
                    label.setStyleName(Style.BOOK_NOT_STARTED.className());
                    return label;
                case IN_PROGRESS:
                    label.setValue("IN PROGRESS");
                    label.setStyleName(Style.BOOK_IN_PROGRESS.className());
                    return label;
                case COMPLETED:
                    label.setValue("COMPLETED");
                    label.setStyleName(Style.BOOK_COMPLETED.className());
                    return label;
                default:
                    throw new UnsupportedOperationException("Unsupported enum entry: " + progress);
            }
        }
    }
}
