/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.data.Binder;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.datefield.DateResolution;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import io.github.aosn.mosaic.domain.model.poll.Book;
import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.issue.IssueService;
import io.github.aosn.mosaic.domain.service.notification.NotificationService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.BulkSelector;
import io.github.aosn.mosaic.ui.view.component.GroupComboBox;
import io.github.aosn.mosaic.ui.view.component.IssueTable;
import io.github.aosn.mosaic.ui.view.component.IssueTable.ColumnGroup;
import io.github.aosn.mosaic.ui.view.component.LoginRequiredLabel;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Notifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private static final String PARAM_GROUP_INDEX = "mosaic.new.group.index";
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final IssueService issueService;
    private transient final PollService pollService;
    private transient final NotificationService notificationService;
    private int selectingGroupIndex = 0;

    @Autowired
    public NewPollView(I18N i18n, UserService userService, IssueService issueService,
                       PollService pollService, NotificationService notificationService) {
        this.i18n = i18n;
        this.userService = userService;
        this.issueService = issueService;
        this.pollService = pollService;
        this.notificationService = notificationService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Integer groupIndex = (Integer) VaadinSession.getCurrent().getAttribute(PARAM_GROUP_INDEX);
        selectingGroupIndex = groupIndex == null ? 0 : groupIndex;
        List<Group> groups;
        try {
            groups = pollService.getAllGroup();
        } catch (RuntimeException e) {
            ErrorView.show(i18n.get("common.error.unexpected"), e);
            return;
        }
        Group selectedGroup = groups.get(selectingGroupIndex);
        List<GitHubIssue> issues;
        try {
            issues = issueService.getOpenIssues(selectedGroup);
        } catch (RuntimeException e) {
            ErrorView.show(i18n.get("common.error.issue.obtain.failed"), e);
            return;
        }
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, selectedGroup, createPollLayout(groups, issues)));
    }

    private Layout createPollLayout(List<Group> groups, List<GitHubIssue> issues) {
        ContentPane contentPane = new ContentPane();

        Group selectedGroup = groups.get(selectingGroupIndex);

        GroupComboBox groupComboBox = new GroupComboBox(i18n.get("new.caption.group"), groups, selectingGroupIndex);
        groupComboBox.addValueChangeListener(e -> {
            VaadinSession.getCurrent().setAttribute(PARAM_GROUP_INDEX, groupComboBox.getSelectIndex());
            UI.getCurrent().getPage().reload();
        });
        FormLayout groupWrapper = new FormLayout(groupComboBox);
        groupWrapper.setMargin(false);
        contentPane.addComponent(groupWrapper);

        List<IssueTable.Row> rows = issues.stream()
                .map(r -> IssueTable.Row.from(r,
                        l -> issueService.isIssueLabel(l, selectedGroup),
                        l -> issueService.trimPartLabel(l, selectedGroup)))
                .collect(Collectors.toList());
        contentPane.addComponent(new IssueTable(i18n.get("new.caption.select"), ColumnGroup.NEW, rows, i18n));
        contentPane.addComponent(new BulkSelector(i18n, rows));

        FormLayout form = new FormLayout();
        form.setMargin(false);
        form.setCaption(i18n.get("new.caption.poll.info"));
        contentPane.addComponent(form);

        LocalDate now = LocalDate.now();
        User user = userService.getUser();
        Poll poll = Poll.create(user, now);
        Binder<Poll> pollBinder = new Binder<>();
        pollBinder.readBean(poll);

        TextField subjectField = new TextField(i18n.get("new.caption.subject"));
        subjectField.setRequiredIndicatorVisible(true);
        subjectField.setWidth(100, Unit.PERCENTAGE);
        subjectField.setPlaceholder(i18n.get("new.placeholder.subject"));
        pollBinder.forField(subjectField)
                .withValidator(new StringLengthValidator("common.validator.text.length.over", 0, 255))
                .bind(Poll::getSubject, Poll::setSubject);
        form.addComponent(subjectField);

        DateField closeDateField = new DateField(i18n.get("new.caption.close.date"));
        closeDateField.setRequiredIndicatorVisible(true);
        closeDateField.setRangeStart(now);
        closeDateField.setResolution(DateResolution.DAY);
        closeDateField.setDateOutOfRangeMessage(i18n.get("common.validator.date.range.over"));
        closeDateField.setValue(poll.getEndAsLocalDate());
        pollBinder.forField(closeDateField).bind(Poll::getEndAsLocalDate, Poll::setEnd);
        form.addComponent(closeDateField);

        ComboBox<Integer> votesComboBox = new ComboBox<>(i18n.get("new.caption.doubles"));
        votesComboBox.setEmptySelectionAllowed(false);
        votesComboBox.setTextInputAllowed(false);
        votesComboBox.setRequiredIndicatorVisible(true);
        votesComboBox.setItems(IntStream.rangeClosed(1, 3).boxed().collect(Collectors.toList()));
        votesComboBox.setValue(poll.getDoubles());
        pollBinder.forField(votesComboBox).bind(Poll::getDoubles, Poll::setDoubles);
        form.addComponent(votesComboBox);

        CheckBox notifyCheckBox = new CheckBox(i18n.get("common.caption.notify.slack"));
        notifyCheckBox.setValue(selectedGroup.isSlackEnabled());
        notifyCheckBox.setEnabled(selectedGroup.isSlackEnabled());
        notifyCheckBox.setDescription(i18n.get("common.label.not.available"));
        contentPane.addComponent(notifyCheckBox);

        Button cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(FrontView.VIEW_NAME));

        Button submitButton = new Button(i18n.get("new.button.submit"), e -> {
            // Validation
            if (subjectField.isEmpty() || !pollBinder.writeBeanIfValid(poll)) {
                Notifications.showWarning(i18n.get("common.notification.input.required"));
                return;
            }

            // User
            if (user == null) {
                ErrorView.show(i18n.get("common.error.user.missing"), null);
                return;
            }

            // Issues
            List<GitHubIssue> selected = rows.stream()
                    .filter(r -> r.getCheckBox().getValue())
                    .map(IssueTable.Row::getIssueEntity)
                    .collect(Collectors.toList());
            if (selected.size() < 2) {
                Notifications.showWarning(i18n.get("new.notification.select.more.2"));
                return;
            }
            if (poll.getDoubles() > selected.size()) {
                Notifications.showWarning(i18n.get("new.notification.doubles.larger"));
                return;
            }

            // Set books
            poll.setBooks(selected.stream()
                    .map(i -> Book.builder().issue(i.getId()).url(i.getUrl()).build())
                    .collect(Collectors.toList()));
            poll.setGroup(selectedGroup);

            // Submit
            try {
                pollService.create(poll);
                if (notifyCheckBox.getValue()) {
                    notificationService.notifyBeginOfPoll(poll);
                }
                Notifications.showSuccess(i18n.get("new.notification.poll.created"));
                getUI().getNavigator().navigateTo(FrontView.VIEW_NAME);
            } catch (RuntimeException ex) {
                ErrorView.show(i18n.get("new.error.poll.create.failed"), ex);
            }
        });
        submitButton.setIcon(VaadinIcons.MEGAFONE);
        submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        HorizontalLayout buttonArea = new HorizontalLayout(cancelButton, submitButton);
        buttonArea.setSpacing(true);
        contentPane.addComponent(buttonArea);
        if (!userService.isLoggedIn()) {
            submitButton.setEnabled(false);
            submitButton.setDescription(i18n.get("common.caption.login.required"));
            contentPane.addComponent(new LoginRequiredLabel(i18n));
        } else if (!userService.isMember(user.getName(), selectedGroup.getOrganization())) {
            submitButton.setEnabled(false);
            String org = selectedGroup.getOrganization();
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
