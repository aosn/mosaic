/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.issue;

import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.repository.issue.GitHubIssueRepository;
import io.github.aosn.mosaic.domain.repository.issue.GitHubIssueRepository.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mikan
 * @since 0.1
 */
@Service
public class IssueService {

    private final GitHubIssueRepository gitHubIssueRepository;

    @Value("${mosaic.issue.label-a}")
    private String labelA;

    @Value("${mosaic.issue.label-b}")
    private String labelB;

    @Autowired
    public IssueService(GitHubIssueRepository gitHubIssueRepository) {
        this.gitHubIssueRepository = gitHubIssueRepository;
    }

    /**
     * Get all pollable issues from GitHub.
     *
     * @return {@link List} of GitHub issues
     * @throws IssueAccessException if GitHub API returns error
     */
    public List<GitHubIssue> getAll() {
        try {
            return gitHubIssueRepository.getAll(State.OPEN).stream().filter(i -> Stream.of(i.getLabels())
                    .anyMatch(l -> l.getName().equals(labelA) || l.getName().equals(labelB)))
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new IssueAccessException("Failed to obtain GitHub issues.", e);
        }
    }

    /**
     * Resolve GitHub issue by given book.
     *
     * @param poll poll
     * @return resolved {@link Poll}
     * @throws NoSuchElementException if cannot resolved
     */
    public Poll resolveBooks(Poll poll) {
        List<GitHubIssue> issues = gitHubIssueRepository.getAll(null);
        poll.getBooks().forEach(b -> {
            b.setGitHubIssue(issues.stream()
                    .filter(i -> i.getId().equals(b.getIssue()))
                    .findFirst().orElseThrow(NoSuchElementException::new));
            b.setVotes(Math.toIntExact(poll.getVotes().stream()
                    .filter(p -> b.equals(p.getBook())).count()));
        });
        return poll;
    }

    public static class IssueAccessException extends RuntimeException {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

        private IssueAccessException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
