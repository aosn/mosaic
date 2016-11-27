/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import io.github.aosn.mosaic.domain.model.poll.Book;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.issue.IssueService;
import io.github.aosn.mosaic.domain.service.notification.NotificationService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.LoginRequiredLabel;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.Header;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.table.IssueRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Front page.
 *
 * @author mikan
 * @since 0.1
 */
@Slf4j
@SpringView(name = NewPollView.VIEW_NAME, ui = MainUI.class)
public class NewPollView extends CustomComponent implements View {

    static final String VIEW_NAME = "new";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final IssueService issueService;
    private transient final PollService pollService;
    private transient final NotificationService notificationService;

    @Autowired
    public NewPollView(I18N i18n, UserService userService, IssueService issueService, PollService pollService,
                       NotificationService notificationService) {
        this.i18n = i18n;
        this.userService = userService;
        this.issueService = issueService;
        this.pollService = pollService;
        this.notificationService = notificationService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        List<GitHubIssue> issues;
        try {
            issues = issueService.getAll();
        } catch (RuntimeException e) {
            ErrorView.show("Failed to obtain issues.", e);
            return;
        }
        setCompositionRoot(new ViewRoot(new Header(i18n, userService), createPollLayout(issues)));
    }

    private Layout createPollLayout(List<GitHubIssue> issues) {
        ContentPane contentPane = new ContentPane();

        List<IssueRow> rows = issues.stream().map(IssueRow::from).collect(Collectors.toList());
        Table issuesTable = new Table("Select Issues", IssueRow.toContainer(rows));
        issuesTable.setPageLength(0);
        issuesTable.setColumnHeader("checkBox", "Select");
        issuesTable.setColumnHeader("title", "Title");
        issuesTable.setColumnHeader("category", "Part");
        issuesTable.setColumnHeader("user", "User");
        issuesTable.setVisibleColumns("checkBox", "title", "category", "user");
        contentPane.addComponent(issuesTable);

        FormLayout form = new FormLayout();
        form.setCaption("Poll information");
        contentPane.addComponent(form);

        TextField subject = new TextField("Subject");
        subject.setRequired(true);
        subject.setWidth(100, Unit.PERCENTAGE);
        form.addComponent(subject);

        DateField closeDate = new DateField("Close Date");
        Date now = new Date();
        closeDate.setRequired(true);
        closeDate.addValidator(new DateRangeValidator("Out of range", now, null, Resolution.DAY));
        closeDate.setValue(now);
        form.addComponent(closeDate);

        ComboBox votesSelect = new ComboBox("votes / user");
        votesSelect.setNullSelectionAllowed(false);
        votesSelect.setTextInputAllowed(false);
        votesSelect.setRequired(true);
        votesSelect.addItems(Arrays.asList("1", "2", "3"));
        votesSelect.setValue("2");
        form.addComponent(votesSelect);

        CheckBox notifyCheck = new CheckBox("Notify to Slack");
        notifyCheck.setValue(false);
        notifyCheck.setEnabled(false);
        notifyCheck.setDescription("This function is not available yet.");
        contentPane.addComponent(notifyCheck);

        Button cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(FrontView.VIEW_NAME));
        Button submitButton = new Button(i18n.get("new.button.submit"), e -> {
            // Validation
            if (!subject.isValid() || !closeDate.isValid() || !votesSelect.isValid() || subject.isEmpty()) {
                Notification.show("Please input all field.");
                return;
            }

            // Doubles
            int doubles;
            try {
                doubles = Integer.parseInt((String) votesSelect.getValue());
            } catch (RuntimeException ex) {
                ErrorView.show("Internal error", ex);
                return;
            }

            // User
            User user = userService.getUser();
            if (user == null) {
                ErrorView.show("User not found.", null);
                return;
            }

            // Issues
            List<GitHubIssue> selected = rows.stream()
                    .filter(r -> r.getCheckBox().getValue())
                    .map(IssueRow::getIssueEntity)
                    .collect(Collectors.toList());
            if (selected.size() < 2) {
                Notification.show("Please select 2 or more issues.");
                return;
            }

            // Selected - Doubles validation
            if (doubles > selected.size()) {
                Notification.show("Votes per user is larger than number of books.");
            }

            // Submit
            try {
                // Build entities
                List<Book> books = selected.stream()
                        .map(i -> Book.builder()
                                .issue(i.getId())
                                .url(i.getUrl())
                                .build())
                        .collect(Collectors.toList());
                Poll poll = Poll.builder()
                        .subject(subject.getValue())
                        .owner(userService.getUser())
                        .state(Poll.PollState.OPEN)
                        .begin(now)
                        .end(closeDate.getValue())
                        .doubles(doubles)
                        .books(books)
                        .votes(Collections.emptyList())
                        .build();
                pollService.create(poll);
                if (notifyCheck.getValue()) {
                    notificationService.notifyCreatePoll(poll);
                }
                Notification.show("Your poll has been created.", Notification.Type.TRAY_NOTIFICATION);
                getUI().getNavigator().navigateTo(FrontView.VIEW_NAME);
            } catch (RuntimeException ex) {
                ErrorView.show("Failed to create poll.", ex);
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
