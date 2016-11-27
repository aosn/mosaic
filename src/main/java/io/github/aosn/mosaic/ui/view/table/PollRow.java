/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.table;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.ui.view.PollResultView;
import io.github.aosn.mosaic.ui.view.PollingView;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

/**
 * Table row for {@link Poll} entity.
 *
 * @author mikan
 * @see com.vaadin.data.util.BeanItemContainer
 * @since 0.1
 */
@Getter
@Builder
public class PollRow implements Serializable {

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

    public static PollRow from(Poll entity) {
        Long pollId = entity.getId();

        // Start button
        Button voteButton = new Button("Start Voting", FontAwesome.THUMBS_UP);
        voteButton.addClickListener(e -> {
            VaadinSession.getCurrent().setAttribute(PollingView.ATTR_POLL_ID, pollId);
            UI.getCurrent().getNavigator().navigateTo(PollingView.VIEW_NAME);
        });

        // Result button
        Button resultButton = new Button("Result", FontAwesome.BAR_CHART);
        resultButton.addClickListener(e ->
                UI.getCurrent().getNavigator().navigateTo(PollResultView.VIEW_NAME + "/" + pollId));

        // Owner button
        Button ownerButton = new Button("Review", FontAwesome.LIST);
        ownerButton.addClickListener(e ->
                UI.getCurrent().getNavigator().navigateTo(PollResultView.VIEW_NAME + "/" + pollId));

        // Votes count
        int votes = entity.getVotes() == null ? 0 : Math.toIntExact(entity.getVotes().stream()
                .map(v -> v.getUser().getId()).distinct().count());

        // Build
        return PollRow.builder()
                .entity(entity)
                .subject(entity.getSubject())
                .begin(entity.getBegin() != null ? DATE_FORMAT.format(entity.getBegin()) : "")
                .end(entity.getEnd() != null ? DATE_FORMAT.format(entity.getEnd()) : "")
                .votes(votes)
                .voteButton(voteButton)
                .resultButton(resultButton)
                .ownerButton(ownerButton)
                .build();
    }

    public static BeanItemContainer<PollRow> toContainer(List<PollRow> rows) {
        if (rows == null) {
            rows = Collections.emptyList();
        }
        return new BeanItemContainer<>(PollRow.class, rows);
    }
}
