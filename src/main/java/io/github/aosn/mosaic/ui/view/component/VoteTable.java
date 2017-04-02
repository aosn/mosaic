/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Vote;
import lombok.Builder;
import lombok.Getter;
import org.vaadin.spring.i18n.I18N;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author mikan
 * @author 0.1
 */
public class VoteTable extends Grid<VoteTable.Row> {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public VoteTable(String caption, List<Row> rows, I18N i18n) {
        super(Row.class);
        setCaption(caption);
        Column<Row, String> userColumn = addColumn(Row::getUser).setCaption(i18n.get("result.column.user"));
        Column<Row, String> timeColumn = addColumn(Row::getTime).setCaption(i18n.get("result.column.timestamp"));
        Column<Row, String> bookColumn = addColumn(Row::getBook).setCaption(i18n.get("result.column.book"));
        //noinspection unchecked
        setColumnOrder(userColumn, timeColumn, bookColumn);
        sort(timeColumn, SortDirection.ASCENDING);
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
        private final String user;
        private final String time;
        private final String book;

        public static Row from(Vote entity) {
            return Row.builder()
                    .user(entity.getUser().getName())
                    .time(DATE_FORMAT.format(entity.getDate()))
                    .book(entity.getBook().getGitHubIssue().getTitle())
                    .build();
        }
    }
}
