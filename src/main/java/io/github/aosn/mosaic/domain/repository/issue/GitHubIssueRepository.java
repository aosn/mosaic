package io.github.aosn.mosaic.domain.repository.issue;

import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
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

    private static final String RESOURCE_PATH = "https://api.github.com/repos/:owner/:repo/issues";
    private static final String NEW_ISSUE_PAGE = "https://github.com/:owner/:repo/issues/new";
    private final RestTemplate restTemplate;
    private String url;

    @Value("${mosaic.issue.organization}")
    private String organization;

    @Value("${mosaic.issue.repository}")
    private String repository;

    @Autowired
    public GitHubIssueRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void init() {
        url = RESOURCE_PATH.replace(":owner", organization).replace(":repo", repository);
    }

    public List<GitHubIssue> getAll(@Nullable State state) {
        log.debug("getAll: url=" + url + ", state=" + state);
        Map<String, String> params = state != null ?
                Collections.singletonMap("state", state.stateValue) : Collections.emptyMap();
        ResponseEntity<GitHubIssue[]> entity = restTemplate.getForEntity(url, GitHubIssue[].class, params);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            log.error("GitHub error: " + entity.getStatusCodeValue());
            throw new RuntimeException("GitHub error: " + entity.getStatusCodeValue());
        }
        return Arrays.asList(entity.getBody());
    }

    public List<GitHubIssue> getByLabels(List<String> labels, @Nullable State state) {
        log.debug("getByLabel: url=" + url + ", labels=" + labels + ", state=" + state);
        if (labels == null) {
            throw new NullPointerException("label is null.");
        }
        if (labels.isEmpty()) {
            throw new IllegalArgumentException("label is empty.");
        }
        Map<String, String> params = new HashMap<>();
        params.put("labels", labels.stream().collect(Collectors.joining(",")));
        if (state != null) {
            params.put("state", state.stateValue);
        }
        ResponseEntity<GitHubIssue[]> entity = restTemplate.getForEntity(url, GitHubIssue[].class, params);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            log.error("GitHub error: " + entity.getStatusCodeValue());
            throw new RuntimeException("GitHub error: " + entity.getStatusCodeValue());
        }
        return Arrays.asList(entity.getBody());
    }

    public String getNewIssueUrl() {
        return NEW_ISSUE_PAGE.replace(":owner", organization).replace(":repo", repository);
    }

    public enum State {
        OPEN("open"),
        CLOSED("closed");

        private final String stateValue;

        State(String stateValue) {
            this.stateValue = stateValue;
        }
    }
}
