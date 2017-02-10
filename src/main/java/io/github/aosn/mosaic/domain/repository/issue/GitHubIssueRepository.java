/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.issue;

import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import io.github.aosn.mosaic.domain.model.poll.Group;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository of {@link GitHubIssue} entity.
 *
 * @author mikan
 * @since 0.1
 */
@Repository
@Slf4j
public class GitHubIssueRepository {

    private static final String PARAM_OWNER = "owner";
    private static final String PARAM_REPO = "repo";
    private static final String PARAM_STATE = "state";
    private static final String PARAM_LABELS = "labels";
    private static final String RESOURCE_PATH = "https://api.github.com/repos/{" + PARAM_OWNER + "}/{" +
            PARAM_REPO + "}/issues?state={" + PARAM_STATE + "}";
    private static final String QUERY_LABEL = "&?labels={" + PARAM_LABELS + "}";
    private static final String NEW_ISSUE_PAGE = "https://github.com/{" + PARAM_OWNER + "}/{" +
            PARAM_REPO + "}/issues/new";
    private final RestTemplate restTemplate;

    @Autowired
    public GitHubIssueRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get all issues.
     *
     * @param state {@link State} for filter issue state, or {@code null} unfiltered
     * @return {@link List} of {@link GitHubIssue}s
     */
    public List<GitHubIssue> getAll(Group group, @Nullable State state) {
        Map<String, String> params = new HashMap<>(3);
        params.put(PARAM_OWNER, group.getOrganization());
        params.put(PARAM_REPO, group.getRepository());
        params.put(PARAM_STATE, state == null ? State.ALL.stateValue : state.stateValue);
        ResponseEntity<GitHubIssue[]> entity = restTemplate.getForEntity(RESOURCE_PATH, GitHubIssue[].class, params);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            log.error("GitHub error: " + entity.getStatusCodeValue());
            throw new RuntimeException("GitHub error: " + entity.getStatusCodeValue());
        }
        return Arrays.asList(entity.getBody());
    }

    /**
     * Get issues by given labels and state.
     *
     * @param labels {@link List} of labels
     * @param state  {@link State} for filter issue state, or {@code null} unfiltered
     * @return {@link List} of {@link GitHubIssue}s
     */
    public List<GitHubIssue> getByLabels(Group group, List<String> labels, @Nullable State state) {
        if (labels == null) {
            throw new NullPointerException("label is null.");
        }
        if (labels.isEmpty()) {
            throw new IllegalArgumentException("label is empty.");
        }
        Map<String, String> params = new HashMap<>(4);
        params.put(PARAM_OWNER, group.getOrganization());
        params.put(PARAM_REPO, group.getRepository());
        params.put(PARAM_LABELS, labels.stream().collect(Collectors.joining(",")));
        params.put(PARAM_STATE, state == null ? State.ALL.stateValue : state.stateValue);
        ResponseEntity<GitHubIssue[]> entity = restTemplate.getForEntity(RESOURCE_PATH + QUERY_LABEL,
                GitHubIssue[].class, params);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            log.error("GitHub error: " + entity.getStatusCodeValue());
            throw new RuntimeException("GitHub error: " + entity.getStatusCodeValue());
        }
        return Arrays.asList(entity.getBody());
    }

    public String getNewIssueUrl(Group group) {
        return NEW_ISSUE_PAGE.replace("{" + PARAM_OWNER + "}", group.getOrganization())
                .replace("{" + PARAM_REPO + "}", group.getRepository());
    }

    public enum State {
        OPEN("open"),
        CLOSED("closed"),
        ALL("all");

        private final String stateValue;

        State(String stateValue) {
            this.stateValue = stateValue;
        }
    }
}
