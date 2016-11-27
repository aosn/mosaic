/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.auth;

import io.github.aosn.mosaic.domain.model.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository of {@link User} entity.
 *
 * @author mikan
 * @see JpaRepository
 * @since 0.1
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByNameAndSource(String name, User.Source source);
}
