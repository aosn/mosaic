/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Book;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.model.poll.Vote;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.issue.IssueService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.HeadingLabel;
import io.github.aosn.mosaic.ui.view.component.IssueTable;
import io.github.aosn.mosaic.ui.view.component.LoginRequiredLabel;
import io.github.aosn.mosaic.ui.view.component.PollTable;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.IconAndName;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Notifications;
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
            ErrorView.show(i18n.get("common.error.parameter.missing"), null);
            return;
        }

        // Search DB
        Poll poll = ErrorView.showIfExceptionThrows(() -> issueService.resolveBooks(pollService.get(pollId)));
        if (poll == null) {
            return;
        }

        // Check status
        if (poll.isClosed()) {
            ErrorView.show(i18n.get("polling.error.closed"), null);
            return;
        }

        // Check already voted
        if (poll.isVoted(userService.getUser())) {
            ErrorView.show(i18n.get("polling.error.voted"), null);
            return;
        }

        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, poll.getGroup(), createPollingLayout(poll)));
    }

    private Layout createPollingLayout(Poll poll) {
        ContentPane contentPane = new ContentPane();

        contentPane.addComponent(new HeadingLabel(i18n.get("polling.label.subject.prefix") + " " +
                poll.getSubject()));

        FormLayout aboutForm = new FormLayout();
        aboutForm.setMargin(false);
        contentPane.addComponent(aboutForm);

        IconAndName organizationLabel = new IconAndName(poll.getGroup());
        organizationLabel.setCaption(i18n.get("result.caption.poll.organization"));
        aboutForm.addComponent(organizationLabel);

        IconAndName ownerLabel = new IconAndName(poll.getOwner());
        ownerLabel.setCaption(i18n.get("result.caption.poll.owner"));
        aboutForm.addComponent(ownerLabel);

        String begin = poll.getBegin() == null ? "?" : PollTable.Row.DATE_FORMAT.format(poll.getBegin());
        String end = poll.getEnd() == null ? "?" : PollTable.Row.DATE_FORMAT.format(poll.getEnd());
        Label termLabel = new Label(begin + " - " + end);
        termLabel.setCaption(i18n.get("result.caption.poll.term"));
        aboutForm.addComponent(termLabel);

        int doubles = poll.getDoubles();
        String doublesCaption;
        String tableCaption;
        if (poll.getDoubles() == 1) {
            doublesCaption = i18n.get("polling.label.doubles.1").replace("%d", Integer.toString(doubles));
            tableCaption = i18n.get("polling.caption.books.1");
        } else {
            doublesCaption = i18n.get("polling.label.doubles.n").replace("%d", Integer.toString(doubles));
            tableCaption = i18n.get("polling.caption.books.n");
        }
        contentPane.addComponent(new Label(doublesCaption));

        List<IssueTable.Row> rows = poll.getBooks().stream()
                .map(r -> IssueTable.Row.from(r,
                        l -> issueService.isIssueLabel(l, poll.getGroup()),
                        l -> issueService.trimPartLabel(l, poll.getGroup())))
                .collect(Collectors.toList());
        contentPane.addComponent(new IssueTable(tableCaption, IssueTable.ColumnGroup.OPEN, rows, i18n));

        Button cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(FrontView.VIEW_NAME));
        Button submitButton = new Button(i18n.get("polling.button.submit"), VaadinIcons.THUMBS_UP);
        submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        submitButton.addClickListener(e -> {
            // Selection
            List<Book> selected = rows.stream()
                    .filter(r -> r.getCheckBox().getValue()).map(IssueTable.Row::getBookEntity)
                    .collect(Collectors.toList());

            // Validation
            if (selected.size() < doubles) {
                int under = doubles - selected.size();
                Notifications.showWarning((under == 1 ? i18n.get("polling.notification.books.under.1") :
                        i18n.get("polling.notification.books.under.n")).replace("%d", Integer.toString(under)));
                return;
            } else if (selected.size() > doubles) {
                int over = selected.size() - doubles;
                Notifications.showWarning((over == 1 ? i18n.get("polling.notification.books.over.1") :
                        i18n.get("polling.notification.books.over.n")).replace("%d", Integer.toString(over)));
                return;
            }

            // Check
            if (poll.getVotes().stream().anyMatch(v -> v.getUser().equals(userService.getUser()))) {
                ErrorView.show(i18n.get("polling.error.voted"), null);
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
                Notifications.showSuccess(i18n.get("polling.notification.vote.submitted"));
                getUI().getNavigator().navigateTo(FrontView.VIEW_NAME);
            } catch (RuntimeException ex) {
                ErrorView.show(i18n.get("polling.error.vote.failed"), ex);
            }
        });
        HorizontalLayout buttonArea = new HorizontalLayout(cancelButton, submitButton);
        buttonArea.setSpacing(true);
        contentPane.addComponent(buttonArea);
        if (!userService.isLoggedIn()) {
            submitButton.setEnabled(false);
            contentPane.addComponent(new LoginRequiredLabel(i18n));
        } else if (!userService.isMember(userService.getName(), poll.getGroup().getOrganization())) {
            submitButton.setEnabled(false);
            String org = poll.getGroup().getOrganization();
            String message = String.format(i18n.get("common.caption.member.only"),
                    "<a href=\"https://github.com/" + org + "\">" + org + "</a>");
            submitButton.setDescription(message, ContentMode.HTML);
            Label notAMemberLabel = new Label(message, ContentMode.HTML);
            notAMemberLabel.setStyleName(ValoTheme.LABEL_FAILURE);
            contentPane.addComponent(notAMemberLabel);
        }

        return contentPane;
    }
}
