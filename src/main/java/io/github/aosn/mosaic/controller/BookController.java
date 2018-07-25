/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.controller;

import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.domain.service.stock.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author mikan
 * @since 0.3
 */
@RestController
@Slf4j
public class BookController {

    private final StockService stockService;

    public BookController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping(value = "/book")
    @ResponseBody
    public void addBook(Stock stock, HttpServletResponse response) {
        try {
            stockService.add(stock);
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
