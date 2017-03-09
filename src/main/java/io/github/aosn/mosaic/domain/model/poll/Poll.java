/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.poll;

import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import lombok.*;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Poll entity.
 *
 * @author mikan
 * @since 0.1
 */
@Entity
@Table(name = "polls")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class Poll implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @Id
    @Column
    @GeneratedValue
    @Getter
    private Long id;

    @Column(nullable = false)
    @Getter
    private String subject;

    @JoinColumn(nullable = false)
    @ManyToOne
    @Getter
    private User owner;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Getter
    @Setter
    private PollState state;

    @Column
    @Temporal(TemporalType.DATE)
    @Getter
    private Date begin;

    @Column
    @Temporal(TemporalType.DATE)
    @Getter
    @Setter
    private Date end;

    @Column(nullable = false)
    @Getter
    private Integer doubles;

    @JoinColumn
    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Getter
    @Setter
    private List<Book> books;

    @JoinColumn
    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Getter
    @Setter
    private List<Vote> votes;

    @JoinColumn
    @OneToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @Getter
    @Setter
    @Nullable
    private Book winBook;

    /**
     * Poll group (issue repository).
     *
     * @since 0.2
     */
    @JoinColumns({@JoinColumn(name = "organization"), @JoinColumn(name = "repository")})
    @ManyToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @Getter
    @Setter
    @Nullable
    private Group group;

    public Book judgeWinner() {
        Map<Book, Integer> votesMap = new HashMap<>();
        AtomicInteger max = new AtomicInteger(0);
        votes.stream().collect(Collectors.groupingBy(Vote::getBook)).forEach((k, v) -> {
            votesMap.put(k, v.size());
            max.set(Math.max(max.get(), v.size()));
        });
        // duplicate check
        LongAdder maxCount = new LongAdder();
        AtomicReference<Book> winner = new AtomicReference<>();
        votesMap.forEach((k, v) -> {
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

    public boolean isOwner(User user) {
        return user != null && owner.equals(user);
    }

    public boolean isVoted(User user) {
        return votes.stream().anyMatch(v -> v.getUser().equals(user));
    }

    public boolean isResultAccessible(User user) {
        return state == PollState.CLOSED || isVoted(user) || isOwner(user);
    }

    public boolean isClosed() {
        return state == PollState.CLOSED;
    }

    public List<User> getVoters() {
        return votes.stream().map(Vote::getUser).distinct().collect(Collectors.toList());
    }

    /**
     * Calculate the popularity rate by book.
     *
     * @param book book for rate
     * @return popularity rate, or zero if no votes in this poll
     */
    public PopularityRate calcPopularityRate(Book book) {
        if (book == null || !books.contains(book)) {
            return new PopularityRate(0, 0);
        }
        return new PopularityRate(book.getVotes(), getVoters().size());
    }

    public enum PollState {
        @SuppressWarnings("unused")PRE_OPEN, // currently unused yet
        OPEN,
        CLOSED
    }

    @Getter
    @AllArgsConstructor
    public static class PopularityRate {
        private final int votes;
        private final int voters;

        double getRate() {
            if (votes == 0) {
                return 0;
            }
            return (double) votes / voters;
        }

        @Override
        public String toString() {
            return String.format("%.1f%% (%d/%d)", getRate() * 100, votes, voters);
        }
    }
}