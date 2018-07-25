/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.auth;

import io.github.aosn.mosaic.domain.model.auth.GitHubOrganization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

/**
 * Repository of {@link GitHubOrganization} entity.
 *
 * @author mikan
 * @since 0.5
 */
@Repository
@Slf4j
public class GitHubOrganizationRepository {

    private static final String RESOURCE_PATH = "https://api.github.com/user/orgs";

    /**
     * Get all joined organizations.
     *
     * @return {@link List} of {@link GitHubOrganization}s
     */
    public List<GitHubOrganization> getAll(OAuth2RestTemplate restTemplate) {
        if (restTemplate == null) {
            throw new NullPointerException("restTemplate is null.");
        }
        log.info("GET " + restTemplate.getUriTemplateHandler().expand(RESOURCE_PATH));
        ResponseEntity<GitHubOrganization[]> entity = restTemplate.getForEntity(RESOURCE_PATH, GitHubOrganization[].class);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            log.error("GitHub error: " + entity.getStatusCodeValue());
            throw new RuntimeException("GitHub error: " + entity.getStatusCodeValue());
        }
        return Arrays.asList(entity.getBody());
    }
}
