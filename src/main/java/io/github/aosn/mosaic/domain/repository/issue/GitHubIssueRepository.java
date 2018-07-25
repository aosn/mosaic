/*
 * Copyright (C) 2016-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.issue;

import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import io.github.aosn.mosaic.domain.model.poll.Group;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Repository of {@link GitHubIssue} entity.
 *
 * @author mikan
 * @since 0.1
 */
@Repository
@Slf4j
public class GitHubIssueRepository {

    private enum Param {
        OWNER, REPO, STATE, PAGE
    }

    private static final String RESOURCE_PATH = "https://api.github.com/repos/" +
            "{" + Param.OWNER + "}/{" + Param.REPO + "}/issues?" +
            "state={" + Param.STATE + "}" +
            "&page={" + Param.PAGE + "}";
    private static final String NEW_ISSUE_PAGE = "https://github.com/" +
            "{" + Param.OWNER + "}/{" +
            Param.REPO + "}/issues/new";
    private final RestTemplate restTemplate;

    public GitHubIssueRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get all issues.
     *
     * @return {@link List} of {@link GitHubIssue}s
     */
    public List<GitHubIssue> getAll(Group group) {
        return getWithState(group, State.ALL);
    }

    /**
     * Get issues with specified state.
     *
     * @param state {@link State} target issue state
     * @return {@link List} of {@link GitHubIssue}s
     */
    public List<GitHubIssue> getWithState(Group group, State state) {
        var overall = new LinkedList<GitHubIssue>();
        for (int page = 1; ; page++) { // Retrieves all page
            var part = retrievePage(group, state, page);
            if (part.length == 0) {
                break;
            }
            overall.addAll(Arrays.asList(part));
        }
        return overall;
    }

    public String getNewIssueUrl(Group group) {
        return NEW_ISSUE_PAGE.replace("{" + Param.OWNER + "}", group.getOrganization())
                .replace("{" + Param.REPO + "}", group.getRepository());
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

    private GitHubIssue[] retrievePage(Group group, State state, int page) {
        var params = Map.of(
                Param.OWNER.name(), group.getOrganization(),
                Param.REPO.name(), group.getRepository(),
                Param.STATE.name(), state.stateValue,
                Param.PAGE.name(), Integer.toString(page));
        log.info("GET " + restTemplate.getUriTemplateHandler().expand(RESOURCE_PATH, params));
        var entity = restTemplate.getForEntity(RESOURCE_PATH, GitHubIssue[].class, params);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            log.error("GitHub error: " + entity.getStatusCodeValue());
            throw new RuntimeException("GitHub error: " + entity.getStatusCodeValue());
        }
        return entity.getBody();
    }
}
