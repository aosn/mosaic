/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Book;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.model.poll.Vote;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.issue.IssueService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.HeadingLabel;
import io.github.aosn.mosaic.ui.view.component.LoginRequiredLabel;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.Header;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.table.IssueRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>Required session parameter:</p>
 * <ul>
 * <li>{@link #ATTR_POLL_ID} - A poll id as {@link Long}</li>
 * </ul>
 *
 * @author mikan
 * @since 0.1
 */
@SpringView(name = PollingView.VIEW_NAME, ui = MainUI.class)
public class PollingView extends CustomComponent implements View {

    public static final String VIEW_NAME = "vote";
    public static final String ATTR_POLL_ID = "mosaic.poll.id";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final IssueService issueService;

    @Autowired
    public PollingView(I18N i18n, UserService userService, PollService pollService, IssueService issueService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.issueService = issueService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Long pollId = (Long) VaadinSession.getCurrent().getAttribute(ATTR_POLL_ID);
        if (pollId == null) {
            ErrorView.show("Parameter missing.", null);
            return;
        }
        Poll poll;
        try {
            poll = issueService.resolveBooks(pollService.get(pollId));
        } catch (IssueService.IssueAccessException e) {
            ErrorView.show("API Error", e);
            return;
        } catch (RuntimeException e) {
            ErrorView.show("DB Error", e);
            return;
        }

        // Check status
        if (poll.getState() != Poll.PollState.OPEN) {
            ErrorView.show("This poll was closed.", null);
            return;
        }

        // Check already voted
        if (poll.getVotes().stream().anyMatch(v -> v.getUser().equals(userService.getUser()))) {
            ErrorView.show("You have already voted.", null);
            return;
        }

        setCompositionRoot(new ViewRoot(new Header(i18n, userService), createPollingLayout(poll)));
    }

    private Layout createPollingLayout(Poll poll) {
        ContentPane contentPane = new ContentPane();

        contentPane.addComponent(new HeadingLabel("Poll of " + poll.getSubject()));
        int doubles = poll.getDoubles();
        String doublesCaption;
        String tableCaption;
        if (poll.getDoubles() == 1) {
            doublesCaption = i18n.get("polling.caption.doubles.1").replace("%d", Integer.toString(doubles));
            tableCaption = i18n.get("polling.caption.books.1");
        } else {
            doublesCaption = i18n.get("polling.caption.doubles.n").replace("%d", Integer.toString(doubles));
            tableCaption = i18n.get("polling.caption.books.n");
        }
        contentPane.addComponent(new Label(doublesCaption));

        List<IssueRow> rows = poll.getBooks().stream().map(IssueRow::from).collect(Collectors.toList());
        Table issuesTable = new Table(tableCaption, IssueRow.toContainer(rows));
        issuesTable.setPageLength(0);
        issuesTable.setColumnHeader("checkBox", "Select");
        issuesTable.setColumnHeader("title", "Title");
        issuesTable.setColumnHeader("category", "Part");
        issuesTable.setVisibleColumns("checkBox", "title", "category");
        contentPane.addComponent(issuesTable);

        Button cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(FrontView.VIEW_NAME));
        Button submitButton = new Button(i18n.get("polling.button.submit"), FontAwesome.THUMBS_UP);
        submitButton.addClickListener(e -> {
            // Selection
            List<Book> selected = rows.stream()
                    .filter(r -> r.getCheckBox().getValue()).map(IssueRow::getBookEntity)
                    .collect(Collectors.toList());

            // Validation
            if (selected.size() < doubles) {
                int under = doubles - selected.size();
                Notification.show("Please select more " + under + " book" + (under == 1 ? "." : "s."));
                return;
            } else if (selected.size() > doubles) {
                int over = selected.size() - doubles;
                Notification.show("Please remove more " + over + " book" + (over == 1 ? "." : "s."));
                return;
            }

            // Check
            if (poll.getVotes().stream().anyMatch(v -> v.getUser().equals(userService.getUser()))) {
                ErrorView.show("You have already voted.", null);
                return;
            }

            Date now = new Date();
            List<Vote> votes = selected.stream().map(i -> Vote.builder()
                    .date(now)
                    .user(userService.getUser())
                    .book(i)
                    .poll(poll)
                    .build()).collect(Collectors.toList());
            try {
                pollService.submit(poll, votes);
                Notification.show("Your vote has been submitted.", Notification.Type.TRAY_NOTIFICATION);
                getUI().getNavigator().navigateTo(FrontView.VIEW_NAME);
            } catch (RuntimeException ex) {
                ErrorView.show("Failed to submit vote(s).", ex);
            }
        });
        HorizontalLayout buttonArea = new HorizontalLayout(cancelButton, submitButton);
        buttonArea.setSpacing(true);
        contentPane.addComponent(buttonArea);
        if (!userService.isLoggedIn()) {
            submitButton.setEnabled(false);
            contentPane.addComponent(new LoginRequiredLabel(i18n));
        }

        return contentPane;
    }
}
