/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.PollTable;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.aosn.mosaic.ui.view.component.PollTable.ColumnGroup.*;

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
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(), createFrontLayout()));
    }

    private Layout createFrontLayout() {
        ContentPane contentPane = new ContentPane();

        // Retrieve poll data
        List<PollTable.Row> open, closed;
        try {
            Map<Poll.PollState, List<PollTable.Row>> openAndClosed = pollService.getAll()
                    .map(p -> PollTable.Row.from(p, userService.getUser(), i18n))
                    .collect(Collectors.groupingBy(p -> p.getEntity().getState()));
            open = openAndClosed.getOrDefault(Poll.PollState.OPEN, Collections.emptyList());
            closed = openAndClosed.getOrDefault(Poll.PollState.CLOSED, Collections.emptyList());
        } catch (RuntimeException e) {
            log.error("getOpenPolls: ", e);
            open = Collections.emptyList();
            closed = Collections.emptyList();
        }

        // Open polls section
        if (open.isEmpty()) {
            Label label = new Label(FontAwesome.INFO_CIRCLE.getHtml() + " " +
                    i18n.get("front.label.poll.open.empty"), ContentMode.HTML);
            contentPane.addComponent(label);
            contentPane.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        } else {
            // Display open polls
            contentPane.addComponent(new PollTable(i18n.get("front.caption.poll.open"), OPENING, open, i18n));
        }

        // Closed polls section
        if (closed.isEmpty()) {
            Label label = new Label(FontAwesome.INFO_CIRCLE.getHtml() + " " +
                    i18n.get("front.label.poll.closed.empty"), ContentMode.HTML);
            contentPane.addComponent(label);
            contentPane.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        } else {
            // Display closed polls
            contentPane.addComponent(new PollTable(i18n.get("front.caption.poll.closed"), CLOSED, closed, i18n));
        }

        // New poll button
        Button newPollButton = new Button(i18n.get("front.button.poll.new"),
                e -> getUI().getNavigator().navigateTo(NewPollView.VIEW_NAME));
        newPollButton.setIcon(FontAwesome.BULLHORN);
        contentPane.addComponent(newPollButton);

        // Owners
        List<PollTable.Row> owners = open.stream()
                .filter(r -> r.getEntity().isOwner(userService.getUser()))
                .collect(Collectors.toList());
        if (!owners.isEmpty()) {
            contentPane.addComponent(new PollTable(i18n.get("front.caption.poll.owner"), OWNER, open, i18n));
        }

        return contentPane;
    }
}
