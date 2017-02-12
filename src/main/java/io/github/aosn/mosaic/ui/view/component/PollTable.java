/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.ui.view.PollResultView;
import io.github.aosn.mosaic.ui.view.PollingView;
import io.github.aosn.mosaic.ui.view.style.Style;
import lombok.Builder;
import lombok.Getter;
import org.vaadin.spring.i18n.I18N;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * A {@link Table} for {@link Poll} entity.
 *
 * @author mikan
 * @since 0.1
 */
public class PollTable extends Table {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    /**
     * Constructs a table.
     *
     * @param caption     caption
     * @param columnGroup visible columnGroup
     * @param rows        row collection
     * @param i18n        message source
     */
    public PollTable(String caption, ColumnGroup columnGroup, List<Row> rows, I18N i18n) {
        super(caption, new BeanItemContainer<>(Row.class, rows));
        setColumnReorderingAllowed(false);
        setSortEnabled(false);
        setSortContainerPropertyId("begin");
        setSortAscending(false);
        setPageLength(0);
        setStyleName(Style.POLLS_TABLE.className());
        setColumnHeader("subject", i18n.get("front.column.poll.subject"));
        setColumnHeader("begin", i18n.get("front.column.poll.begin"));
        setColumnHeader("end", i18n.get("front.column.poll.end"));
        setColumnHeader("votes", i18n.get("front.column.poll.votes"));
        setColumnHeader("voteButton", i18n.get("front.column.poll.vote"));
        setColumnHeader("resultButton", i18n.get("front.column.poll.result"));
        setColumnHeader("ownerButton", i18n.get("front.column.poll.review"));
        setVisibleColumns((Object[]) columnGroup.columns);
    }

    public enum ColumnGroup {
        OPENING("subject", "begin", "votes", "voteButton"),
        CLOSED("subject", "begin", "votes", "resultButton"),
        OWNER("subject", "begin", "votes", "ownerButton");
        private final String[] columns;

        ColumnGroup(String... columns) {
            this.columns = columns;
        }
    }

    /**
     * Table row for {@link Poll} entity.
     *
     * @author mikan
     * @see com.vaadin.data.util.BeanItemContainer
     * @since 0.1
     */
    @Getter
    @Builder
    public static class Row implements Serializable {

        public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
        private transient final Poll entity;
        private final String subject;
        private final String begin;
        private final String end;
        private final int votes;
        private final Button voteButton;
        private final Button resultButton;
        private final Button ownerButton;

        public static Row from(Poll entity, User user, I18N i18n) {
            Long pollId = entity.getId();

            // Start button or progress button
            Button voteOrProgressButton;
            if (entity.isVoted(user)) {
                voteOrProgressButton = new Button(i18n.get("common.button.poll.progress"), FontAwesome.BAR_CHART);
                voteOrProgressButton.addClickListener(e ->
                        UI.getCurrent().getNavigator().navigateTo(PollResultView.VIEW_NAME + "/" + pollId));
            } else {
                voteOrProgressButton = new Button(i18n.get("common.button.poll.start"), FontAwesome.THUMBS_UP);
                voteOrProgressButton.addClickListener(e -> {
                    VaadinSession.getCurrent().setAttribute(PollingView.ATTR_POLL_ID, pollId);
                    UI.getCurrent().getNavigator().navigateTo(PollingView.VIEW_NAME);
                });
                if (user != null) {
                    voteOrProgressButton.setStyleName(ValoTheme.BUTTON_FRIENDLY);
                }
            }

            // Result button
            Button resultButton = new Button(i18n.get("front.button.poll.result"), FontAwesome.BAR_CHART);
            resultButton.addClickListener(e ->
                    UI.getCurrent().getNavigator().navigateTo(PollResultView.VIEW_NAME + "/" + pollId));

            // Owner button
            Button ownerButton = new Button(i18n.get("front.button.poll.review"), FontAwesome.LIST);
            ownerButton.addClickListener(e ->
                    UI.getCurrent().getNavigator().navigateTo(PollResultView.VIEW_NAME + "/" + pollId));

            // Votes count
            int votes = entity.getVotes() == null ? 0 : Math.toIntExact(entity.getVotes().stream()
                    .map(v -> v.getUser().getId()).distinct().count());

            // Build
            return Row.builder()
                    .entity(entity)
                    .subject(entity.getSubject())
                    .begin(entity.getBegin() != null ? DATE_FORMAT.format(entity.getBegin()) : "")
                    .end(entity.getEnd() != null ? DATE_FORMAT.format(entity.getEnd()) : "")
                    .votes(votes)
                    .voteButton(voteOrProgressButton)
                    .resultButton(resultButton)
                    .ownerButton(ownerButton)
                    .build();
        }
    }
}
