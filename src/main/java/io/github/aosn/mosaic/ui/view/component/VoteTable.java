/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Table;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Vote;
import lombok.Builder;
import lombok.Getter;
import org.vaadin.spring.i18n.I18N;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * @author mikan
 * @author 0.1
 */
public class VoteTable extends Table {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public VoteTable(String caption, List<Row> rows, I18N i18n) {
        super(caption, new BeanItemContainer<>(Row.class, rows));
        setPageLength(0);
        setColumnHeader("user", "User");
        setColumnHeader("time", "Timestamp");
        setColumnHeader("book", "Book");
        setVisibleColumns("user", "time", "book");
    }

    /**
     * @author mikan
     * @since 0.1
     */
    @Getter
    @Builder
    public static class Row implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
        private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        private transient final Vote entity;
        private final String user;
        private final String time;
        private final String book;

        public static Row from(Vote entity) {
            return Row.builder()
                    .entity(entity)
                    .user(entity.getUser().getName())
                    .time(DATE_FORMAT.format(entity.getDate()))
                    .book(entity.getBook().getGitHubIssue().getTitle())
                    .build();
        }

        public static BeanItemContainer<Row> toContainer(List<Row> rows) {
            if (rows == null) {
                rows = Collections.emptyList();
            }
            return new BeanItemContainer<>(Row.class, rows);
        }
    }
}
