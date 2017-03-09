/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.stock;

import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.catalog.ReleasedBook;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import io.github.aosn.mosaic.domain.repository.stock.StockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides {@link Stock} and {@link ReleasedBook} entity operations.
 *
 * @author mikan
 * @since 0.3
 */
@Service
@Slf4j
public class StockService {

    private final StockRepository stockRepository;

    @Autowired
    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /**
     * Get all entities.
     *
     * @return {@link List} of entities
     * @throws DataAccessException if the database error occurred
     */
    public List<Stock> getAll(User user) {
        return stockRepository.findByUser(user);
    }

    /**
     * Get a entry by id.
     *
     * @param id stock id
     * @return {@link Stock} object, or null if not found
     * @throws DataAccessException if the database error occurred
     */
    public Stock get(long id) {
        return stockRepository.findOne(id);
    }

    /**
     * Add a stock.
     *
     * @param stock stock
     * @throws IllegalArgumentException the book is already stocked
     * @throws DataAccessException      if the database error occurred
     */
    public void add(Stock stock) {
        if (stockRepository.findOne(stock.getId()) != null) {
            throw new IllegalArgumentException("Book already stocked: " + stock.getId());
        }
        stockRepository.saveAndFlush(stock);
    }

    /**
     * Update a stock.
     *
     * @param stock stock
     * @throws IllegalArgumentException the book isn't stocked
     * @throws DataAccessException      if the database error occurred
     */
    public void update(Stock stock) {
        if (stockRepository.findOne(stock.getId()) == null) {
            throw new IllegalArgumentException("No such book: " + stock.getId());
        }
        stockRepository.saveAndFlush(stock);
    }
}
