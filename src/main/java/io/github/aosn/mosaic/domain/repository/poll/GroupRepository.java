/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.poll;

import io.github.aosn.mosaic.domain.model.poll.Group;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository of {@link Group} entity.
 *
 * @author mikan
 * @see JpaRepository
 * @see DataAccessException
 * @since 0.2
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Group.GroupKey> {
}
