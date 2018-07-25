/*
 * Copyright (C) 2016-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.auth;

import io.github.aosn.mosaic.domain.model.auth.GitHubOrganization;
import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.domain.repository.auth.GitHubOrganizationRepository;
import io.github.aosn.mosaic.domain.repository.auth.UserRepository;
import io.github.aosn.mosaic.domain.repository.issue.GitHubIssueRepository;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author mikan
 * @since 0.1
 */
@Service
@Slf4j
public class UserService {

    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final GitHubIssueRepository issueRepository;
    private final GitHubOrganizationRepository groupRepository;
    private static Map<String, Set<String>> userGroupCache = new ConcurrentHashMap<>(); // WARNING: instance-local

    public UserService(UserRepository userRepository, GitHubOrganizationRepository groupRepository,
                       GitHubIssueRepository issueRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.issueRepository = issueRepository;
    }

    public void recordLogin(Principal principal, User.Source source, OAuth2RestTemplate restTemplate) {
        var now = new Date();
        log.info("recordLogin: " + principal);
        var user = userRepository.findByNameAndSource(principal.getName(), source);
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
            cacheGroups(principal.getName(), restTemplate);
        } catch (RuntimeException e) {
            log.error("recordLogin: failed to persist.", e);
        }
    }

    public boolean isLoggedIn() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_USER));
    }

    public String getName() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @Nullable
    public User getUser() {
        try {
            return userRepository.findByNameAndSource(getName(), User.Source.GITHUB);
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

    /**
     * Cache user groups.
     *
     * @param userName     user name
     * @param restTemplate OAuth2 Rest Template
     * @since 0.5
     */
    private void cacheGroups(String userName, OAuth2RestTemplate restTemplate) {
        try {
            var groups = groupRepository.getAll(restTemplate).stream()
                    .map(GitHubOrganization::getOrganization)
                    .collect(Collectors.toSet());
            log.info("ORGS: user=" + userName + " orgs=" + String.join(",", groups));
            userGroupCache.put(userName, groups);
        } catch (RuntimeException e) {
            log.warn("ORGS: Cannot check membership", e);
        }
    }

    /**
     * Either group (= organization) member or not.
     *
     * @param userName    user name
     * @param targetGroup target group
     * @return {@code true} if user is a member of group, {@code false} otherwise
     * @since 0.5
     */
    public boolean isMember(String userName, String targetGroup) {
        return userGroupCache.getOrDefault(userName, Collections.emptySet()).contains(targetGroup);
    }
}
