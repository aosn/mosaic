/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.google.common.base.Strings;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.poll.Book;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.issue.IssueService;
import io.github.aosn.mosaic.domain.service.notification.NotificationService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.*;
import io.github.aosn.mosaic.ui.view.component.IssueTable.ColumnGroup;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.IconAndName;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Notifications;
import io.github.aosn.mosaic.ui.view.style.Style;
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
            ErrorView.show(i18n.get("common.error.parameter.missing"), null);
            return;
        }
        long pollId;
        try {
            pollId = Stream.of(event.getParameters().split("/"))
                    .mapToLong(Long::parseLong)
                    .findFirst().orElseThrow(NoSuchElementException::new);
        } catch (RuntimeException e) {
            ErrorView.show(i18n.get("common.error.parameter.missing"), e);
            return;
        }

        // Search DB
        Poll poll = ErrorView.showIfExceptionThrows(() -> issueService.resolveBooks(pollService.get(pollId)));
        if (poll == null) {
            return;
        }

        // Access check
        if (!poll.isResultAccessible(userService.getUser())) {
            ErrorView.show(i18n.get("result.error.access.forbidden"), null);
            return;
        }

        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, poll.getGroup(), createResultLayout(poll)));
    }

    private Layout createResultLayout(Poll poll) {
        ContentPane contentPane = new ContentPane();

        contentPane.addComponent(new HeadingLabel(i18n.get("result.label.subject.prefix") + " " +
                poll.getSubject()));

        FormLayout aboutForm = new FormLayout();
        aboutForm.setMargin(false);
        contentPane.addComponent(aboutForm);

        IconAndName ownerLabel = new IconAndName(poll.getOwner());
        ownerLabel.setCaption(i18n.get("result.caption.poll.owner"));
        aboutForm.addComponent(ownerLabel);

        String begin = poll.getBegin() == null ? "?" : PollTable.Row.DATE_FORMAT.format(poll.getBegin());
        String end = poll.getEnd() == null ? "?" : PollTable.Row.DATE_FORMAT.format(poll.getEnd());
        Label termLabel = new Label(begin + " - " + end);
        termLabel.setCaption(i18n.get("result.caption.poll.term"));
        aboutForm.addComponent(termLabel);

        List<User> users = poll.getVoters();
        Label votesPerUserLabel = new Label(String.valueOf(users.size()));
        votesPerUserLabel.setCaption(i18n.get("result.caption.poll.voters.n"));
        aboutForm.addComponent(votesPerUserLabel);

        CssLayout voters = new CssLayout();
        voters.setStyleName(Style.USER_COLLECTION.className());
        voters.setCaption(i18n.get("result.caption.poll.voters.list"));
        users.forEach(u -> voters.addComponent(new IconAndName(u)));
        aboutForm.addComponent(voters);

        // Show winner when poll was closed
        if (poll.getState() == Poll.PollState.CLOSED) {
            Book winner = poll.getWinBook();
            Label winnerLabel = new Label(winner == null ? i18n.get("result.label.poll.winner.tie") :
                    winner.getGitHubIssue().getTitle());
            winnerLabel.setCaption(i18n.get("result.caption.poll.winner"));
            aboutForm.addComponent(winnerLabel);

            // Calculate popularity rate
            if (winner != null) {
                Label popularityRateLabel = new Label(poll.calcPopularityRate(winner).toString());
                popularityRateLabel.setCaption(i18n.get("result.caption.popularity"));
                aboutForm.addComponent(popularityRateLabel);
            }
        }

        List<IssueTable.Row> rows = poll.getBooks().stream()
                .map(r -> IssueTable.Row.from(r,
                        l -> issueService.isIssueLabel(l, poll.getGroup()),
                        l -> issueService.trimPartLabel(l, poll.getGroup())))
                .collect(Collectors.toList());
        contentPane.addComponent(new IssueTable(i18n.get("result.caption.book.list"), ColumnGroup.CLOSED, rows,
                i18n));

        Button backButton = new Button(i18n.get("common.button.back"),
                e -> getUI().getPage().setLocation(MainUI.PATH));
        contentPane.addComponent(backButton);
        contentPane.setComponentAlignment(backButton, Alignment.MIDDLE_CENTER);

        // Owner's view
        if (poll.isOwner(userService.getUser())) {
            contentPane.addComponent(new HeadingLabel(i18n.get("result.label.owner.operation")));

            FormLayout summaryForm = new FormLayout();
            summaryForm.setMargin(false);
            contentPane.addComponent(summaryForm);

            Book winner = poll.judgeWinner();
            String winnerName = winner == null ? i18n.get("result.label.poll.winner.tie") :
                    winner.getGitHubIssue().getTitle();
            Label winnerLabel = new Label(winnerName);
            winnerLabel.setCaption(i18n.get("result.label.poll.winner.current"));
            summaryForm.addComponent(winnerLabel);

            if (winner != null) {
                Label popularityRateLabel = new Label(poll.calcPopularityRate(winner).toString());
                popularityRateLabel.setCaption(i18n.get("result.caption.popularity"));
                summaryForm.addComponent(popularityRateLabel);
            }

            CheckBox notifyCheck = new CheckBox(i18n.get("common.caption.notify.slack"));
            notifyCheck.setValue(false);
            notifyCheck.setEnabled(false);
            contentPane.addComponent(notifyCheck);

            String confirmMessage = i18n.get("result.label.confirm.close") + "<br/>" +
                    i18n.get("result.label.poll.winner.current") + ": " + winnerName;
            Button closeButton = new Button(i18n.get("result.button.poll.close"),
                    e -> UI.getCurrent().addWindow(new ConfirmWindow(confirmMessage, i18n, ok -> {
                        pollService.close(poll);
                        if (notifyCheck.getValue()) {
                            notificationService.notifyClosePoll(poll);
                        }
                        Notifications.showNormal(i18n.get("result.notification.poll.closed"));
                        getUI().getNavigator().navigateTo(FrontView.VIEW_NAME);
                    })));
            closeButton.setStyleName(ValoTheme.BUTTON_DANGER);
            contentPane.addComponent(closeButton);
            if (poll.isClosed()) {
                closeButton.setEnabled(false);
                notifyCheck.setEnabled(false);
                closeButton.setDescription(i18n.get("polling.error.closed"));
            }

            contentPane.addComponent(new VoteTable(i18n.get("result.caption.votes"), poll.getVotes().stream()
                    .map(VoteTable.Row::from).collect(Collectors.toList()), i18n));
        }

        return contentPane;
    }
}
