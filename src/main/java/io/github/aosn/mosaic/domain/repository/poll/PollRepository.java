/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.poll;

import io.github.aosn.mosaic.domain.model.poll.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository of {@link Poll} entity.
 *
 * @author mikan
 * @see JpaRepository
 * @since 0.1
 */
@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
}
