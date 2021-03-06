/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.LoginRequiredLabel;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Notifications;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.vaadin.spring.i18n.I18N;

import java.io.Serializable;

/**
 * @author mikan
 * @since 0.2
 */
@Slf4j
@SpringView(name = NewGroupView.VIEW_NAME, ui = MainUI.class)
public class NewGroupView extends CustomComponent implements View {

    static final String VIEW_NAME = "new-group";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;

    public NewGroupView(I18N i18n, UserService userService, PollService pollService) {
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(), createNewGroupLayout()));
    }

    private Layout createNewGroupLayout() {
        var contentPane = new ContentPane();

        var defaultGroup = pollService.getDefaultGroup();
        var thinGroup = new GroupBean(userService.getUser(), defaultGroup);

        var form = new FormLayout();
        form.setCaption(i18n.get("new-group.caption.title"));
        form.setMargin(false);
        contentPane.addComponent(form);
        var groupBinder = new Binder<GroupBean>();
        groupBinder.readBean(thinGroup);

        var organizationField = new TextField();
        organizationField.setCaption(i18n.get("new-group.caption.organization"));
        organizationField.setPlaceholder(defaultGroup.getOrganization());
        organizationField.setRequiredIndicatorVisible(true);
        groupBinder.forField(organizationField).bind(GroupBean::getOrganization, GroupBean::setOrganization);
        form.addComponent(organizationField);

        var repositoryField = new TextField();
        repositoryField.setCaption(i18n.get("new-group.caption.repository"));
        repositoryField.setPlaceholder(defaultGroup.getRepository());
        repositoryField.setRequiredIndicatorVisible(true);
        groupBinder.forField(repositoryField).bind(GroupBean::getRepository, GroupBean::setRepository);
        form.addComponent(repositoryField);

        var labelFilterField = new TextField();
        labelFilterField.setCaption(i18n.get("new-group.caption.label.filter"));
        labelFilterField.setPlaceholder(defaultGroup.getLabelFilter());
        labelFilterField.setRequiredIndicatorVisible(true);
        groupBinder.forField(labelFilterField)
                .withValidator((v, c) -> v.contains(Group.LABEL_FILTER_PATTERN) ? ValidationResult.ok() :
                        ValidationResult.error(String.format(i18n.get("new-group.validator.pattern.contains"),
                                " \"" + Group.LABEL_FILTER_PATTERN + "\"")))
                .bind(GroupBean::getLabelFilter, GroupBean::setLabelFilter);
        form.addComponent(labelFilterField);

        var slackWebhookField = new TextField();
        slackWebhookField.setCaption("Webhook");
        slackWebhookField.setPlaceholder("https://hooks.slack.com/services/...");
        groupBinder.forField(slackWebhookField).bind(GroupBean::getWebhook, GroupBean::setWebhook);
        form.addComponent(slackWebhookField);

        var slackChannelField = new TextField();
        slackChannelField.setCaption("Channel");
        slackChannelField.setPlaceholder("general");
        groupBinder.forField(slackChannelField).bind(GroupBean::getChannel, GroupBean::setChannel);
        form.addComponent(slackChannelField);

        var slackUsernameField = new TextField();
        slackUsernameField.setCaption("Username");
        slackUsernameField.setPlaceholder("Mosaic");
        groupBinder.forField(slackUsernameField).bind(GroupBean::getUsername, GroupBean::setUsername);
        form.addComponent(slackUsernameField);

        var slackBeginTemplate = new TextField();
        slackBeginTemplate.setWidth(100, Unit.PERCENTAGE);
        slackBeginTemplate.setCaption("Begin message");
        slackBeginTemplate.setPlaceholder("%s has started. Let's vote!\nhttps://vote.aosn.ws/");
        groupBinder.forField(slackBeginTemplate).bind(GroupBean::getBeginTemplate, GroupBean::setBeginTemplate);
        form.addComponent(slackBeginTemplate);

        var slackEndTemplate = new TextField();
        slackEndTemplate.setWidth(100, Unit.PERCENTAGE);
        slackEndTemplate.setCaption("Begin message");
        slackEndTemplate.setPlaceholder("%s has been ended. Please check result.\nhttps://vote.aosn.ws/");
        groupBinder.forField(slackEndTemplate).bind(GroupBean::getEndTemplate, GroupBean::setEndTemplate);
        form.addComponent(slackEndTemplate);

        var cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(FrontView.VIEW_NAME));
        var submitButton = new Button(i18n.get("new-group.button.create"), e -> {
            if (!groupBinder.writeBeanIfValid(thinGroup)) {
                Notifications.showWarning(i18n.get("common.notification.input.required"));
                return;
            }
            if (!userService.isMember(userService.getName(), organizationField.getValue())) {
                Notifications.showWarning(String.format(i18n.get("common.caption.member.only"),
                        organizationField.getValue()));
                return;
            }
            try {
                pollService.addGroup(thinGroup.toGroup());
                Notifications.showSuccess(i18n.get("new-group.notification.add.success"));
                getUI().getNavigator().navigateTo(NewPollView.VIEW_NAME);
            } catch (IllegalArgumentException ex) {
                Notifications.showWarning(ex.getMessage());
            } catch (RuntimeException ex) {
                ErrorView.show(i18n.get("new-group.error.add.failed"), ex);
            }
        });
        submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

        var buttonArea = new HorizontalLayout(cancelButton, submitButton);
        buttonArea.setSpacing(true);
        contentPane.addComponent(buttonArea);
        if (!userService.isLoggedIn()) {
            submitButton.setEnabled(false);
            submitButton.setDescription(i18n.get("common.caption.login.required"));
            contentPane.addComponent(new LoginRequiredLabel(i18n));
        }

        return contentPane;
    }

    @Getter
    @Setter
    private static class GroupBean implements Serializable {
        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
        private User owner;
        private String organization, repository, labelFilter;
        private String webhook, channel, username, iconEmoji, beginTemplate, endTemplate;

        GroupBean(User owner, Group defaultGroup) {
            this.owner = owner;
            this.organization = defaultGroup.getOrganization();
            this.repository = defaultGroup.getRepository();
            this.labelFilter = defaultGroup.getLabelFilter();
        }

        Group toGroup() {
            return new Group(organization, repository, labelFilter, owner, Group.SlackParam.builder()
                    .webhook(webhook).channel(channel).username(username)
                    .beginTemplate(beginTemplate).endTemplate(endTemplate).build());
        }
    }
}
