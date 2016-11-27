/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.table;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import io.github.aosn.mosaic.domain.model.issue.GitHubLabel;
import io.github.aosn.mosaic.domain.model.poll.Book;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.pegdown.PegDownProcessor;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author mikan
 * @since 0.1
 */
@Getter
@Builder
public class IssueRow implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private transient final GitHubIssue issueEntity;
    @Nullable
    private transient final Book bookEntity;
    private final CheckBox checkBox;
    private final Label title;
    private final Label category;
    private final Label user;
    private final int votes;
    private final Label votesWithIcon;

    public static IssueRow from(Book entity) {
        String titleText = "#" + entity.getId() + " " + entity.getGitHubIssue().getTitle();
        Label title = new Label(createAnchor(entity.getUrl(), titleText), ContentMode.HTML);
        title.setStyleName("link");
        title.setDescription(markdownToHtml(entity.getGitHubIssue().getBody()));
        return IssueRow.builder()
                .bookEntity(entity)
                .issueEntity(entity.getGitHubIssue())
                .checkBox(new CheckBox())
                .title(title)
                .category(createCategory(entity.getGitHubIssue().getLabels()))
                .user(new Label(entity.getGitHubIssue().getUser().getName()))
                .votes(entity.getVotes())
                .votesWithIcon(new Label(IntStream.range(0, entity.getVotes())
                        .mapToObj(i -> FontAwesome.THUMBS_UP.getHtml())
                        .collect(Collectors.joining()), ContentMode.HTML))
                .build();
    }

    public static IssueRow from(GitHubIssue entity) {
        String titleText = "#" + entity.getId() + " " + entity.getTitle();
        Label title = new Label(createAnchor(entity.getUrl(), titleText), ContentMode.HTML);
        title.setStyleName("link");
        title.setDescription(markdownToHtml(entity.getBody()));
        return IssueRow.builder()
                .issueEntity(entity)
                .checkBox(new CheckBox())
                .title(title)
                .category(createCategory(entity.getLabels()))
                .user(new Label(entity.getUser().getName()))
                .build();
    }

    public static BeanItemContainer<IssueRow> toContainer(List<IssueRow> rows) {
        if (rows == null) {
            rows = Collections.emptyList();
        }
        return new BeanItemContainer<>(IssueRow.class, rows);
    }

    private static String createAnchor(String url, String title) {
        return "<a href=\"" + url + "\" target=\"_blank\">" + title + "</a>";
    }

    private static Label createCategory(GitHubLabel[] labels) {
        String label = Stream.of(labels)
                .map(l -> "<span style=\"color:white;background:#" + l.getColor() +
                        ";border-radius:0.3em;padding: 0 0.5em;\">" + trimPartLabel(l.getName()) + "</span>")
                .collect(Collectors.joining());
        return new Label(label, ContentMode.HTML);
    }

    private static String trimPartLabel(String label) {
        if (label.contains("A")) {
            return "A";
        }
        if (label.contains("B")) {
            return "B";
        }
        return label;
    }

    private static String markdownToHtml(String markdown) {
        return new PegDownProcessor().markdownToHtml(markdown);
    }
}
