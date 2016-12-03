/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
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
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Poll implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @Id
    @Column
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String subject;

    @JoinColumn(nullable = false)
    @ManyToOne
    private User owner;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Setter
    private PollState state;

    @Column
    @Temporal(TemporalType.DATE)
    private Date begin;

    @Column
    @Temporal(TemporalType.DATE)
    @Setter
    private Date end;

    @Column(nullable = false)
    private Integer doubles;

    @JoinColumn
    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Setter
    private List<Book> books;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @JoinColumn
    @OneToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @Setter
    private List<Vote> votes;

    @JoinColumn
    @OneToOne
    @NotFound(action = NotFoundAction.IGNORE)
    @Setter
    @Nullable
    private Book winBook;

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
        return user != null && owner.getId().equals(user.getId());
    }

    public boolean isAccessible(User user) {
        return user != null && state != PollState.CLOSED && !owner.equals(user);
    }

    public boolean isClosed() {
        return state == PollState.CLOSED;
    }

    public enum PollState {
        PRE_OPEN,
        OPEN,
        CLOSED
    }
}