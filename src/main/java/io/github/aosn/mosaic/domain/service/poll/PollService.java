/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.poll;

import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.model.poll.Vote;
import io.github.aosn.mosaic.domain.repository.poll.PollRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

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

    @Autowired
    public PollService(PollRepository pollRepository) {
        this.pollRepository = pollRepository;
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
}
