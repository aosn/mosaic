/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.poll;

import io.github.aosn.mosaic.domain.model.poll.Group;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.model.poll.Vote;
import io.github.aosn.mosaic.domain.repository.poll.GroupRepository;
import io.github.aosn.mosaic.domain.repository.poll.PollRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * Provides {@link Poll} entity operations.
 *
 * @author mikan
 * @since 0.1
 */
@Service
@Slf4j
public class PollService {

    private static final Lock LOCK = new ReentrantLock();
    private final PollRepository pollRepository;
    private final GroupRepository groupRepository;

    @Value("${mosaic.issue.organization}")
    private String defaultOrganization;

    @Value("${mosaic.issue.repository}")
    private String defaultRepository;

    @Value("${mosaic.issue.filter}")
    private String defaultLabelFilter;

    @Autowired
    public PollService(PollRepository pollRepository, GroupRepository groupRepository) {
        this.pollRepository = pollRepository;
        this.groupRepository = groupRepository;
    }

    @PostConstruct
    private void init() {
        // insert default record at first time
        List<Group> allInDataSource = groupRepository.findAll();
        Group group = new Group(defaultOrganization, defaultRepository, defaultLabelFilter, null);
        if (allInDataSource.isEmpty()) {
            log.info("Inserting default group: " + group);
            groupRepository.saveAndFlush(group);
            pollRepository.findAll().stream().filter(p -> p.getGroup() == null).forEach(p -> {
                p.setGroup(group);
                pollRepository.save(p);
            });
            pollRepository.flush();
            return;
        }
        // check default value modification
        Group defaultGroup = allInDataSource.stream()
                .filter(g -> g.getOwner() == null) // null for default
                .findFirst().orElseThrow(IllegalStateException::new);
        if (!group.equals(defaultGroup)) {
            // update record
            log.info("Updating default group:\nfrom\t" + defaultGroup + "\nto\t" + group);
            defaultGroup.replace(group);
            groupRepository.saveAndFlush(defaultGroup);
        }
    }

    /**
     * Get all entities.
     *
     * @return {@link Stream} of entities
     * @throws DataAccessException if the database error occurred
     */
    public Stream<Poll> getAll() {
        return pollRepository.findAll().stream();
    }

    /**
     * Get a poll entity.
     *
     * @param pollId poll id
     * @return entity
     * @throws NoSuchElementException if requested poll is not found
     * @throws DataAccessException    if the database error occurred
     */
    public Poll get(Long pollId) {
        Poll poll = pollRepository.findOne(pollId);
        if (poll == null) {
            throw new NoSuchElementException("Poll not found: " + pollId);
        }
        return poll;
    }

    /**
     * Submit votes.
     *
     * @param poll  poll
     * @param votes {@link List} of {@link Vote}s
     * @throws DataAccessException if the database error occurred
     */
    @Transactional
    public void submit(Poll poll, List<Vote> votes) {
        log.info("BEGIN submit: " + votes);
        LOCK.lock();
        try {
            List<Vote> allVotes = poll.getVotes();
            allVotes.addAll(votes);
            poll.setVotes(allVotes);
            pollRepository.saveAndFlush(poll);
        } finally {
            LOCK.unlock();
        }
        log.info("END submit: " + votes);
    }

    /**
     * Create a poll.
     *
     * @param poll entity
     * @throws DataAccessException if the database error occurred
     */
    @Transactional
    public void create(Poll poll) {
        log.info("BEGIN create: " + poll);
        pollRepository.saveAndFlush(poll);
        log.info("END create: " + poll);
    }

    /**
     * Close a poll.
     *
     * @param poll poll
     * @throws DataAccessException if the database error occurred
     */
    @Transactional
    public void close(Poll poll) {
        log.info("BEGIN close: " + poll);
        poll.setState(Poll.PollState.CLOSED);
        poll.setEnd(new Date());
        poll.setWinBook(poll.judgeWinner());
        pollRepository.saveAndFlush(poll);
        log.info("END close: " + poll);
    }

    /**
     * Get all groups or singleton list that contains default group.
     *
     * @return list of group
     * @throws DataAccessException   if the database error occurred
     * @throws IllegalStateException if default entry is missing
     */
    public List<Group> getAllGroup() {
        List<Group> groups = groupRepository.findAll();
        if (groups.isEmpty()) {
            throw new IllegalStateException("Missing default entry");
        }
        return groups;
    }

    /**
     * Get default group.
     *
     * @return default group
     * @throws DataAccessException   if the database error occurred
     * @throws IllegalStateException if default entry is missing
     */
    public Group getDefaultGroup() {
        Group group = groupRepository.findOne(new Group.GroupKey(defaultOrganization, defaultRepository));
        if (group == null) {
            throw new IllegalStateException("Missing default entry");
        }
        return group;
    }

    public void addGroup(Group group) {
        if (groupRepository.findAll().stream().anyMatch(g -> g.equals(group))) {
            throw new IllegalArgumentException("Group already registered");
        }
        groupRepository.saveAndFlush(group);
        log.info("Group added: " + group);
    }

    /**
     * Count number of polls registered.
     *
     * @return number of polls
     * @since 0.3
     */
    public long countPolls() {
        return pollRepository.count();
    }

    /**
     * Count number of groups registered.
     *
     * @return number of groups
     * @since 0.3
     */
    public long countGroups() {
        return groupRepository.count();
    }
}
