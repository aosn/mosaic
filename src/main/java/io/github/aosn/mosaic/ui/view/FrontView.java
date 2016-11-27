/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.Header;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.table.PollRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Front page.
 *
 * @author mikan
 * @since 0.1
 */
@Slf4j
@SpringView(name = FrontView.VIEW_NAME, ui = MainUI.class)
public class FrontView extends CustomComponent implements View {

    static final String VIEW_NAME = "";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;

    @Autowired
    public FrontView(I18N i18n, UserService userService, PollService pollService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setCompositionRoot(new ViewRoot(new Header(i18n, userService), createFrontLayout()));
    }

    private Layout createFrontLayout() {
        ContentPane contentPane = new ContentPane();

        // Retrieve poll data
        List<PollRow> open = null;
        List<PollRow> closed = null;
        try {
            Map<Poll.PollState, List<PollRow>> openAndClosed = pollService.getAll()
                    .map(PollRow::from)
                    .collect(Collectors.groupingBy(p -> p.getEntity().getState()));
            open = openAndClosed.get(Poll.PollState.OPEN);
            closed = openAndClosed.get(Poll.PollState.CLOSED);
        } catch (RuntimeException e) {
            log.error("getOpeningPolls: ", e);
        }
        if (open == null) {
            open = Collections.emptyList();
        }
        if (closed == null) {
            closed = Collections.emptyList();
        }
        if (open.isEmpty()) {
            Label label = new Label("No opening polls.");
            contentPane.addComponent(label);
            contentPane.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        } else {
            // Opening Polls
            Table openingPollsTable = new Table("Opening polls", PollRow.toContainer(open));
            openingPollsTable.setColumnReorderingAllowed(false);
            openingPollsTable.setSortEnabled(false);
            openingPollsTable.setSortContainerPropertyId("begin");
            openingPollsTable.setSortAscending(false);
            openingPollsTable.setPageLength(0);
            openingPollsTable.setStyleName("polls-table");
            openingPollsTable.setColumnHeader("subject", "Subject");
            openingPollsTable.setColumnHeader("begin", "Begin");
            openingPollsTable.setColumnHeader("votes", "Votes");
            openingPollsTable.setColumnHeader("voteButton", "Vote");
            openingPollsTable.setVisibleColumns("subject", "begin", "votes", "voteButton");
            contentPane.addComponent(openingPollsTable);
        }

        if (closed.isEmpty()) {
            Label label = new Label("No closed polls.");
            contentPane.addComponent(label);
            contentPane.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        } else {
            // Closed Polls
            Table closedPollsTable = new Table("Closed polls", PollRow.toContainer(closed));
            closedPollsTable.setColumnReorderingAllowed(false);
            closedPollsTable.setSortEnabled(false);
            closedPollsTable.setSortContainerPropertyId("begin");
            closedPollsTable.setSortAscending(false);
            closedPollsTable.setPageLength(0);
            closedPollsTable.setStyleName("polls-table");
            closedPollsTable.setColumnHeader("subject", "Subject");
            closedPollsTable.setColumnHeader("begin", "Begin");
            closedPollsTable.setColumnHeader("votes", "Votes");
            closedPollsTable.setColumnHeader("resultButton", "Result");
            closedPollsTable.setVisibleColumns("subject", "begin", "votes", "resultButton");
            contentPane.addComponent(closedPollsTable);
        }

        // New poll
        Button newPollButton = new Button(i18n.get("front.button.poll.new"),
                e -> getUI().getNavigator().navigateTo(NewPollView.VIEW_NAME));
        newPollButton.setIcon(FontAwesome.BULLHORN);
        contentPane.addComponent(newPollButton);

        // Owners
        List<PollRow> owners = open.stream()
                .filter(r -> r.getEntity().getOwner().equals(userService.getUser()))
                .collect(Collectors.toList());
        if (!owners.isEmpty()) {
            Table ownersTable = new Table("Owner polls", PollRow.toContainer(open));
            ownersTable.setColumnReorderingAllowed(false);
            ownersTable.setSortEnabled(false);
            ownersTable.setSortContainerPropertyId("begin");
            ownersTable.setSortAscending(false);
            ownersTable.setPageLength(0);
            ownersTable.setStyleName("polls-table");
            ownersTable.setColumnHeader("subject", "Subject");
            ownersTable.setColumnHeader("begin", "Begin");
            ownersTable.setColumnHeader("end", "End");
            ownersTable.setColumnHeader("votes", "Votes");
            ownersTable.setColumnHeader("ownerButton", "Review");
            ownersTable.setVisibleColumns("subject", "begin", "votes", "ownerButton");
            contentPane.addComponent(ownersTable);
        }

        return contentPane;
    }
}
