/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.Table;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import io.github.aosn.mosaic.domain.model.issue.GitHubLabel;
import io.github.aosn.mosaic.domain.model.poll.Book;
import io.github.aosn.mosaic.ui.view.style.Style;
import lombok.Builder;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.pegdown.PegDownProcessor;
import org.vaadin.spring.i18n.I18N;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author mikan
 * @since 0.1
 */
public class IssueTable extends Table {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public IssueTable(String caption, ColumnGroup columnGroup, List<Row> rows, I18N i18n) {
        super(caption, new BeanItemContainer<>(Row.class, rows));
        setPageLength(0);
        setColumnHeader("checkBox", i18n.get("new.column.select"));
        setColumnHeader("title", i18n.get("new.column.title"));
        setColumnHeader("category", i18n.get("new.column.part"));
        setColumnHeader("user", i18n.get("new.column.user"));
        setColumnHeader("votesWithIcon", i18n.get("polling.column.votes"));
        setColumnHeader("votes", i18n.get("polling.column.count"));
        setVisibleColumns((Object[]) columnGroup.columns);
        if (columnGroup == ColumnGroup.CLOSED) {
            setSortContainerPropertyId("votes");
            setSortAscending(false);
        }
    }

    public enum ColumnGroup {
        NEW("checkBox", "title", "category", "user"),
        OPEN("checkBox", "title", "category", "user"),
        CLOSED("title", "category", "votesWithIcon", "votes"),;
        private final String[] columns;

        ColumnGroup(String... columns) {
            this.columns = columns;
        }
    }

    /**
     * @author mikan
     * @since 0.1
     */
    @Getter
    @Builder
    public static class Row implements Serializable {

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

        public static Row from(Book entity, @Nullable Predicate<GitHubLabel> partFilter,
                               @Nullable UnaryOperator<String> labelTrimmer) {
            String titleText = "#" + entity.getGitHubIssue().getId() + " " + entity.getGitHubIssue().getTitle();
            Label title = new Label(createAnchor(entity.getUrl(), titleText), ContentMode.HTML);
            title.setStyleName(Style.LINK.className());
            title.setDescription(markdownToHtml(entity.getGitHubIssue().getBody()), ContentMode.HTML);
            return Row.builder()
                    .bookEntity(entity)
                    .issueEntity(entity.getGitHubIssue())
                    .checkBox(new CheckBox())
                    .title(title)
                    .category(createCategory(entity.getGitHubIssue().getLabels(), partFilter, labelTrimmer))
                    .user(new Label(entity.getGitHubIssue().getUser().getName()))
                    .votes(entity.getVotes())
                    .votesWithIcon(new Label(IntStream.range(0, entity.getVotes())
                            .mapToObj(i -> VaadinIcons.THUMBS_UP.getHtml())
                            .collect(Collectors.joining()), ContentMode.HTML))
                    .build();
        }

        public static Row from(GitHubIssue entity, @Nullable Predicate<GitHubLabel> partFilter,
                               @Nullable UnaryOperator<String> labelTrimmer) {
            String titleText = "#" + entity.getId() + " " + entity.getTitle();
            Label title = new Label(createAnchor(entity.getUrl(), titleText), ContentMode.HTML);
            title.setStyleName(Style.LINK.className());
            title.setDescription(markdownToHtml(entity.getBody()), ContentMode.HTML);
            return Row.builder()
                    .issueEntity(entity)
                    .checkBox(new CheckBox())
                    .title(title)
                    .category(createCategory(entity.getLabels(), partFilter, labelTrimmer))
                    .user(new Label(entity.getUser().getName()))
                    .build();
        }

        private static String createAnchor(String url, String title) {
            return "<a href=\"" + url + "\" target=\"_blank\">" + title + "</a>";
        }

        private static Label createCategory(GitHubLabel[] labels, @Nullable Predicate<GitHubLabel> partFilter,
                                            @Nullable UnaryOperator<String> labelTrimmer) {
            Predicate<GitHubLabel> f = partFilter == null ? l -> true : partFilter;
            UnaryOperator<String> t = labelTrimmer == null ? l -> l : labelTrimmer;
            String label = Stream.of(labels)
                    .filter(f)
                    .map(l -> "<span class=\"" + Style.ISSUE_LABEL.className() +
                            "\" style=\"color:white;background:#" + l.getColor() + ";\">" +
                            t.apply(l.getName()) + "</span>")
                    .collect(Collectors.joining());
            return new Label(label, ContentMode.HTML);
        }

        private static String markdownToHtml(String markdown) {
            return new PegDownProcessor().markdownToHtml(markdown);
        }
    }
}
