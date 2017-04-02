/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.poll.PollService;
import io.github.aosn.mosaic.domain.service.stock.StockService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.spring.i18n.I18N;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides some administration information & operations.
 *
 * @author mikan
 * @since 0.3
 */
@SpringView(name = AdminView.VIEW_NAME, ui = MainUI.class)
@Slf4j
public class AdminView extends CustomComponent implements View {

    static final String VIEW_NAME = "admin";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final Environment env;
    private transient final I18N i18n;
    private transient final UserService userService;
    private transient final PollService pollService;
    private transient final StockService stockService;

    @Autowired
    public AdminView(Environment env, I18N i18n, UserService userService, PollService pollService,
                     StockService stockService) {
        this.env = env;
        this.i18n = i18n;
        this.userService = userService;
        this.pollService = pollService;
        this.stockService = stockService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        getUI().getPage().setTitle(i18n.get("header.label.title"));
        setCompositionRoot(new ViewRoot(i18n, userService, pollService.getDefaultGroup(), createAdminLayout()));
    }

    private Layout createAdminLayout() {
        ContentPane contentPane = new ContentPane();

        FormLayout info = new FormLayout();
        info.setCaption("Application information");
        info.setIcon(VaadinIcons.INFO_CIRCLE);
        contentPane.addComponent(info);

        info.addComponent(createFormEntry("App version", "Mosaic " + MosaicApplication.MOSAIC_VERSION));
        info.addComponent(createFormEntry("Serial version", MosaicApplication.MOSAIC_SERIAL_VERSION_UID + "L"));
        info.addComponent(createFormEntry("Profile", Stream.of(env.getActiveProfiles())
                .collect(Collectors.joining(" "))));

        FormLayout stats = new FormLayout();
        stats.setCaption("Statistics");
        stats.setIcon(VaadinIcons.LINE_CHART);
        contentPane.addComponent(stats);

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();

        stats.addComponent(createFormEntry("Uptime", hrTimes(ManagementFactory.getRuntimeMXBean().getUptime())));
        stats.addComponent(createFormEntry("Memory usage", hrBytes(allocatedMemory) + " / " + hrBytes(maxMemory) +
                " (" + String.format("%.1f", (double) allocatedMemory / maxMemory * 100) + "%)"));
        stats.addComponent(createFormEntry("Number of users", Long.toString(userService.countUsers())));
        stats.addComponent(createFormEntry("Number of groups", Long.toString(pollService.countGroups())));
        stats.addComponent(createFormEntry("Number of polls", Long.toString(pollService.countPolls())));
        stats.addComponent(createFormEntry("Number of books", Long.toString(stockService.countStocks())));

        Button newGroupButton = new Button("Create group",
                e -> getUI().getNavigator().navigateTo(NewGroupView.VIEW_NAME));
        newGroupButton.setIcon(VaadinIcons.GROUP);

        Button apiDocsButton = new Button("API Docs",
                e -> getUI().getPage().setLocation("/swagger-ui.html"));
        apiDocsButton.setIcon(VaadinIcons.PUZZLE_PIECE);

        HorizontalLayout buttonArea = new HorizontalLayout(newGroupButton, apiDocsButton);
        buttonArea.setSpacing(true);
        contentPane.addComponent(buttonArea);

        return contentPane;
    }

    private Label createFormEntry(String caption, String text) {
        Label label = new Label(text);
        label.setCaption(caption);
        return label;
    }

    private String hrBytes(long bytes) {
        return FileUtils.byteCountToDisplaySize(bytes);
    }

    private String hrTimes(long times) {
        return new PrettyTime(new Date(times), i18n.getLocale()).format(new Date(0));
    }
}
