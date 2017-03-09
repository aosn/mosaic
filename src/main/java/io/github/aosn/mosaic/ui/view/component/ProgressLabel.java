/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.ui.Label;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.ui.view.style.Style;
import org.vaadin.spring.i18n.I18N;

/**
 * @author mikan
 * @since 0.3
 */
public class ProgressLabel extends Label {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public ProgressLabel(Stock stock, I18N i18n) {
        switch (stock.getProgress()) {
            case NOT_STARTED:
                setValue(i18n.get("book.caption.progress.no"));
                setStyleName(Style.BOOK_NOT_STARTED.className());
                break;
            case IN_PROGRESS:
                setValue(i18n.get("book.caption.progress.began"));
                setStyleName(Style.BOOK_IN_PROGRESS.className());
                break;
            case COMPLETED:
                setValue(i18n.get("book.caption.progress.completed"));
                setStyleName(Style.BOOK_COMPLETED.className());
                break;
            default:
                setValue(stock.getProgressPercentage() + "%");
        }
    }
}
