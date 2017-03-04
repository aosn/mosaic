/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.stock;

import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author mikan
 * @since 0.3
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByUser(User user);
}
