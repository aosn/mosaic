/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.catalog;

import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import io.github.aosn.mosaic.domain.repository.catalog.GoogleBookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides book catalog.
 *
 * @author mikan
 * @since 0.3
 */
@Service
@Slf4j
public class CatalogService {

    private final GoogleBookRepository googleBookRepository;

    public CatalogService(GoogleBookRepository googleBookRepository) {
        this.googleBookRepository = googleBookRepository;
    }

    /**
     * Search the book catalog by ISBN.
     *
     * @param isbn ISBN
     * @return {@link List} of books, or empty if no match.
     * @throws NullPointerException     if isbn is null
     * @throws IllegalArgumentException if isbn is empty
     * @throws HttpClientErrorException if the http error occurred
     * @throws RuntimeException         if unexpected status returned
     */
    public List<ReleasedBook> searchByIsbn(String isbn) {
        if (isbn == null) {
            throw new NullPointerException("isbn is null.");
        }
        if (isbn.isEmpty()) {
            throw new IllegalArgumentException("isbn is empty.");
        }
        // Use Google's repository
        return googleBookRepository.getByIsbn(isbn).stream()
                .map(b -> (ReleasedBook) b).collect(Collectors.toList());
    }

    /**
     * Search the book catalog by keyword.
     *
     * @param keyword keyword
     * @return {@link List} of books, or empty if no match.
     * @throws NullPointerException     if keyword is null
     * @throws IllegalArgumentException if keyword is empty
     * @throws HttpClientErrorException if the http error occurred
     * @throws RuntimeException         if unexpected status returned
     */
    public List<ReleasedBook> searchByKeyword(String keyword) {
        if (keyword == null) {
            throw new NullPointerException("keyword is null.");
        }
        if (keyword.isEmpty()) {
            throw new IllegalArgumentException("keyword is empty.");
        }
        // Use Google's repository
        return googleBookRepository.getByKeyword(keyword).stream()
                .map(b -> (ReleasedBook) b).collect(Collectors.toList());
    }
}
