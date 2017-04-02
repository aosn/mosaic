/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.auth;

import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.domain.repository.auth.UserRepository;
import io.github.aosn.mosaic.domain.repository.issue.GitHubIssueRepository;
import io.github.aosn.mosaic.domain.repository.ui.SessionRepository;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Date;

/**
 * @author mikan
 * @since 0.1
 */
@Service
@Slf4j
public class UserService {

    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final GitHubIssueRepository issueRepository;

    @Autowired
    public UserService(UserRepository userRepository, SessionRepository sessionRepository,
                       GitHubIssueRepository issueRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.issueRepository = issueRepository;
    }

    public void recordLogin(Principal principal, User.Source source) {
        Date now = new Date();
        log.info("recordLogin: " + principal);
        User user = userRepository.findByNameAndSource(principal.getName(), source);
        if (user == null) {
            user = User.builder()
                    .name(principal.getName())
                    .source(source)
                    .firstLogin(now)
                    .build();
        }
        user.setLastLogin(now);
        try {
            userRepository.saveAndFlush(user);
            sessionRepository.setUser(user);
        } catch (RuntimeException e) {
            log.error("recordLogin: failed to persist.", e);
        }
    }

    public boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_USER));
    }

    public String getName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Nullable
    public User getUser() {
        // Get create current session
        User user = sessionRepository.getUser();
        if (user != null) {
            return user;
        }
        // Get create DB when nothing in session
        try {
            user = userRepository.findByNameAndSource(getName(), User.Source.GITHUB);
            sessionRepository.setUser(user);
            return user;
        } catch (RuntimeException e) {
            log.error("getUser failed.", e);
            return null;
        }
    }

    /**
     * Get the new issue url.
     *
     * @param group group
     * @return url
     */
    public String getNewIssueUrl(Group group) {
        return issueRepository.getNewIssueUrl(group);
    }

    /**
     * Count number of users registered.
     *
     * @return number of users
     * @since 0.3
     */
    public long countUsers() {
        return userRepository.count();
    }
}
