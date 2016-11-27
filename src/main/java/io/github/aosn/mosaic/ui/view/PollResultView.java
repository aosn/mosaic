/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.google.common.base.Strings;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Book;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.issue.IssueService;
import io.github.aosn.mosaic.domain.service.notification.NotificationService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.HeadingLabel;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.Header;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.table.IssueRow;
import io.github.aosn.mosaic.ui.view.table.PollRow;
import io.github.aosn.mosaic.ui.view.table.VoteRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>Required path parameter:</p>
 * <ul>
 * <li>{@code /:poll_id} - A poll id as {@link Long}</li>
 * </ul>
 *
 * @author mikan
 * @since 0.1
 */
@SpringView(name = PollResultView.VIEW_NAME, ui = MainUI.class)
public class PollResultView extends CustomComponent implements View {

    public static final String VIEW_NAME = "result";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final IssueService issueService;
    private transient final NotificationService notificationService;

    @Autowired
    public PollResultView(I18N i18n, UserService userService, PollService pollService, IssueService issueService,
                          NotificationService notificationService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.issueService = issueService;
        this.notificationService = notificationService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // Parse parameter
        if (Strings.isNullOrEmpty(event.getParameters())) {
            ErrorView.show("Parameter missing.", null);
            return;
        }
        long pollId;
        try {
            pollId = Stream.of(event.getParameters().split("/"))
                    .mapToLong(Long::parseLong)
                    .findFirst().orElseThrow(NoSuchElementException::new);
        } catch (RuntimeException e) {
            ErrorView.show("Parameter missing.", e);
            return;
        }

        // Search DB
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

        // Access check
        if (pollService.checkOpenPollAccess(poll, userService.getUser())) {
            ErrorView.show("You can not display this result yet.", null);
            return;
        }

        setCompositionRoot(new ViewRoot(new Header(i18n, userService), createResultLayout(poll)));
    }

    private Layout createResultLayout(Poll poll) {
        ContentPane contentPane = new ContentPane();

        contentPane.addComponent(new HeadingLabel("Poll result of " + poll.getSubject()));

        FormLayout aboutForm = new FormLayout();

        Label ownerLabel = new Label(poll.getOwner().getName());
        ownerLabel.setCaption("Started by");
        aboutForm.addComponent(ownerLabel);

        String begin = poll.getBegin() == null ? "?" : PollRow.DATE_FORMAT.format(poll.getBegin());
        String end = poll.getEnd() == null ? "?" : PollRow.DATE_FORMAT.format(poll.getEnd());
        Label termLabel = new Label(begin + " - " + end);
        termLabel.setCaption("Term");
        aboutForm.addComponent(termLabel);

        List<String> users = poll.getVotes().stream()
                .map(v -> v.getUser().getName()).distinct().collect(Collectors.toList());
        Label votesPerUserLabel = new Label(users.size() +
                " (" + users.stream().collect(Collectors.joining(" ")) + ")");
        votesPerUserLabel.setCaption("Voters");
        aboutForm.addComponent(votesPerUserLabel);

        if (poll.getState() == Poll.PollState.CLOSED) {
            Book winBook = poll.getWinBook();
            Label winnerLabel = new Label(winBook == null ? "(tie)" : winBook.getGitHubIssue().getTitle());
            winnerLabel.setCaption("Winner");
            aboutForm.addComponent(winnerLabel);
        }

        contentPane.addComponent(aboutForm);

        List<IssueRow> rows = poll.getBooks().stream()
                .map(IssueRow::from)
                .collect(Collectors.toList());
        Table issuesTable = new Table("List of books", IssueRow.toContainer(rows));
        issuesTable.setPageLength(0);
        issuesTable.setColumnHeader("title", "Title");
        issuesTable.setColumnHeader("category", "Part");
        issuesTable.setColumnHeader("votesWithIcon", "Votes");
        issuesTable.setColumnHeader("votes", "Count");
        issuesTable.setVisibleColumns("title", "category", "votesWithIcon", "votes");
        issuesTable.setSortContainerPropertyId("votes");
        issuesTable.setSortAscending(false);
        contentPane.addComponent(issuesTable);

        Button backButton = new Button(i18n.get("common.button.back"),
                e -> getUI().getPage().setLocation(MainUI.PATH));
        contentPane.addComponent(backButton);
        contentPane.setComponentAlignment(backButton, Alignment.MIDDLE_CENTER);

        if (pollService.checkUserClosable(poll, userService.getUser())) {
            contentPane.addComponent(new HeadingLabel("Owner operation"));

            Book winner = pollService.judgeWinner(poll);
            contentPane.addComponent(new Label("Current winner: " +
                    (winner == null ? "(tie)" : winner.getGitHubIssue().getTitle())));

            CheckBox notifyCheck = new CheckBox("Notify to Slack");
            notifyCheck.setValue(false);
            notifyCheck.setEnabled(false);
            contentPane.addComponent(notifyCheck);

            Button closeButton = new Button("Close", e -> {
                pollService.close(poll);
                if (notifyCheck.getValue()) {
                    notificationService.notifyClosePoll(poll);
                }
                Notification.show("Your poll has been closed.", Notification.Type.TRAY_NOTIFICATION);
                getUI().getNavigator().navigateTo(FrontView.VIEW_NAME);
            });
            contentPane.addComponent(closeButton);

            Table votesTable = new Table("All votes", VoteRow.toContainer(poll.getVotes().stream()
                    .map(VoteRow::from).collect(Collectors.toList())));
            votesTable.setPageLength(0);
            votesTable.setColumnHeader("user", "User");
            votesTable.setColumnHeader("time", "Timestamp");
            votesTable.setColumnHeader("book", "Book");
            votesTable.setVisibleColumns("user", "time", "book");
            contentPane.addComponent(votesTable);
        }

        return contentPane;
    }
}
