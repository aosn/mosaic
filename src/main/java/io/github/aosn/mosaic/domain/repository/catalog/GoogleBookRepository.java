/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.catalog;

import io.github.aosn.mosaic.domain.model.catalog.GoogleBook;
import io.github.aosn.mosaic.domain.model.catalog.GoogleBookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Repository of {@link GoogleBook}.
 *
 * @author mikan
 * @since 0.3
 */
@Repository
@Slf4j
public class GoogleBookRepository {

    private static final String PARAM_Q = "query";
    private static final String RESOURCE_PATH = "https://www.googleapis.com/books/v1/volumes?q=" +
            "{" + PARAM_Q + "}";
    private final RestTemplate restTemplate;

    @Autowired
    public GoogleBookRepository(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get by ISBN.
     *
     * @param isbn ISBN
     * @return {@link List} of {@link GoogleBook} entities.
     * @throws HttpClientErrorException if the http error occurred
     * @throws RuntimeException if unexpected status returned
     */
    public List<GoogleBook> getByIsbn(String isbn) {
        Map<String, String> params = Collections.singletonMap(PARAM_Q, "isbn:" + isbn);
        log.info("GET " + restTemplate.getUriTemplateHandler().expand(RESOURCE_PATH, params));
        ResponseEntity<GoogleBookResponse> entity = restTemplate.getForEntity(RESOURCE_PATH,
                GoogleBookResponse.class, params);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            log.error("Google API error: " + entity.getStatusCodeValue());
            throw new RuntimeException("Google API error: " + entity.getStatusCodeValue());
        }
        return entity.getBody().getItems();
    }

    /**
     * Get by keyword.
     *
     * @param keyword keyword
     * @return {@link List} of {@link GoogleBook} entities.
     * @throws HttpClientErrorException if the http error occurred
     * @throws RuntimeException if unexpected status returned
     */
    public List<GoogleBook> getByKeyword(String keyword) {
        Map<String, String> params = Collections.singletonMap(PARAM_Q, keyword);
        log.info("GET " + restTemplate.getUriTemplateHandler().expand(RESOURCE_PATH, params));
        ResponseEntity<GoogleBookResponse> entity = restTemplate.getForEntity(RESOURCE_PATH,
                GoogleBookResponse.class, params);
        if (!entity.getStatusCode().is2xxSuccessful()) {
            log.error("Google API error: " + entity.getStatusCodeValue());
            throw new RuntimeException("Google API error: " + entity.getStatusCodeValue());
        }
        return entity.getBody().getItems();
    }
}
