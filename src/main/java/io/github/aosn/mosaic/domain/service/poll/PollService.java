/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.poll;

import io.github.aosn.mosaic.domain.model.auth.User;
import io.github.aosn.mosaic.domain.model.poll.Book;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.model.poll.Vote;
import io.github.aosn.mosaic.domain.repository.poll.PollRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
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
     * @throws RuntimeException if database error occurred
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
     * @throws RuntimeException       if database error occurred
     */
    public Poll get(Long pollId) {
        Poll poll = pollRepository.findOne(pollId);
        if (poll == null) {
            throw new NoSuchElementException("Poll not found: " + pollId);
        }
        return poll;
    }

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
     * @throws RuntimeException if database error occurred
     */
    @Transactional
    public void create(Poll poll) {
        log.info("BEGIN create: " + poll);
        pollRepository.saveAndFlush(poll);
        log.info("END create: " + poll);
    }

    @Transactional
    public void close(Poll poll) {
        log.info("BEGIN close: " + poll);
        poll.setState(Poll.PollState.CLOSED);
        poll.setEnd(new Date());
        poll.setWinBook(judgeWinner(poll));
        pollRepository.saveAndFlush(poll);
        log.info("END close: " + poll);
    }

    public boolean checkOpenPollAccess(Poll poll, User user) {
        return poll.getState() != Poll.PollState.CLOSED && !poll.getOwner().equals(user);
    }

    public boolean checkUserClosable(Poll poll, User user) {
        return poll.getState() != Poll.PollState.CLOSED && poll.getOwner().equals(user);
    }

    public Book judgeWinner(Poll poll) {
        Map<Book, Integer> votes = new HashMap<>();
        AtomicInteger max = new AtomicInteger(0);
        poll.getVotes().stream().collect(Collectors.groupingBy(Vote::getBook)).forEach((k, v) -> {
            votes.put(k, v.size());
            max.set(Math.max(max.get(), v.size()));
        });
        // duplicate check
        LongAdder maxCount = new LongAdder();
        AtomicReference<Book> winner = new AtomicReference<>();
        votes.forEach((k, v) -> {
            if (v == max.get()) {
                winner.set(k);
                maxCount.increment();
            }
        });
        if (maxCount.sum() != 1) {
            return null; // duplicate
        }
        return winner.get();
    }
}
