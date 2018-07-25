/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.controller;

import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.domain.service.catalog.CatalogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * @author mikan
 * @since 0.3
 */
@RestController
@Slf4j
public class CatalogController {

    public static final String CATALOG_ENDPOINT = "/catalog";
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping(value = CATALOG_ENDPOINT)
    @ResponseBody
    public List<ReleasedBook> findBooks(@RequestParam("q") String keyword, HttpServletResponse response) {
        try {
            return catalogService.searchByIsbn(Stock.normalizeIsbn(keyword));
        } catch (IllegalArgumentException e) {
            // Not a ISBN, search by name
            return catalogService.searchByKeyword(keyword);
        } catch (NullPointerException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return Collections.emptyList();
    }
}
