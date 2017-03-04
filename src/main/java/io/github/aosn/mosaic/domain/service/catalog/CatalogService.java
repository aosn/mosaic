/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.catalog;

import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import io.github.aosn.mosaic.domain.repository.catalog.GoogleBookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    public CatalogService(GoogleBookRepository googleBookRepository) {
        this.googleBookRepository = googleBookRepository;
    }

    /**
     * Search the book catalog by ISBN.
     *
     * @param isbn ISBN
     * @return {@link List} of books, or empty if no match.
     */
    public List<ReleasedBook> searchByIsbn(String isbn) {
        // Use Google's repository
        return googleBookRepository.getByIsbn(isbn).stream()
                .map(b -> (ReleasedBook) b).collect(Collectors.toList());
    }

    /**
     * Search the book catalog by keyword.
     *
     * @param keyword keyword
     * @return {@link List} of books, or empty if no match.
     */
    public List<ReleasedBook> searchByKeyword(String keyword) {
        // Use Google's repository
        return googleBookRepository.getByKeyword(keyword).stream()
                .map(b -> (ReleasedBook) b).collect(Collectors.toList());
    }
}
