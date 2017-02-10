/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.component.LoginRequiredLabel;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Notifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

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

    @Autowired
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
        ContentPane contentPane = new ContentPane();

        Group defaultGroup = pollService.getDefaultGroup();

        FormLayout form = new FormLayout();
        form.setCaption(i18n.get("new-group.caption.title"));
        form.setMargin(false);
        contentPane.addComponent(form);

        TextField organizationField = new TextField();
        organizationField.setCaption(i18n.get("new-group.caption.organization"));
        organizationField.setInputPrompt(defaultGroup.getOrganization());
        organizationField.setRequired(true);
        form.addComponent(organizationField);

        TextField repositoryField = new TextField();
        repositoryField.setCaption(i18n.get("new-group.caption.repository"));
        repositoryField.setInputPrompt(defaultGroup.getRepository());
        repositoryField.setRequired(true);
        form.addComponent(repositoryField);

        TextField labelFilterField = new TextField();
        labelFilterField.setCaption(i18n.get("new-group.caption.label.filter"));
        labelFilterField.setInputPrompt(defaultGroup.getLabelFilter());
        labelFilterField.addValidator(new AbstractStringValidator(String.format(
                i18n.get("new-group.validator.pattern.contains"), " \"" + Group.LABEL_FILTER_PATTERN + "\"")) {
            private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

            @Override
            protected boolean isValidValue(String value) {
                return value.contains(Group.LABEL_FILTER_PATTERN);
            }
        });
        labelFilterField.setRequired(true);
        form.addComponent(labelFilterField);

        Button cancelButton = new Button(i18n.get("common.button.cancel"),
                e -> getUI().getNavigator().navigateTo(FrontView.VIEW_NAME));
        Button submitButton = new Button(i18n.get("new-group.button.create"), e -> {
            if (!organizationField.isValid() || !repositoryField.isValid() || !labelFilterField.isValid()) {
                Notifications.showWarning(i18n.get("common.notification.input.required"));
                return;
            }
            Group group = new Group(organizationField.getValue(), repositoryField.getValue(),
                    labelFilterField.getValue(), userService.getUser());
            try {
                pollService.addGroup(group);
                Notifications.showNormal(i18n.get("new-group.notification.add.success"));
                getUI().getNavigator().navigateTo(NewPollView.VIEW_NAME);
            } catch (IllegalArgumentException ex) {
                Notifications.showWarning(ex.getMessage());
            } catch (RuntimeException ex) {
                ErrorView.show(i18n.get("new-group.error.add.failed"), ex);
            }
        });
        submitButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

        HorizontalLayout buttonArea = new HorizontalLayout(cancelButton, submitButton);
        buttonArea.setSpacing(true);
        contentPane.addComponent(buttonArea);
        if (!userService.isLoggedIn()) {
            submitButton.setEnabled(false);
            submitButton.setDescription(i18n.get("common.caption.login.required"));
            contentPane.addComponent(new LoginRequiredLabel(i18n));
        }

        return contentPane;
    }
}
