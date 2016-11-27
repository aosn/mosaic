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
import java.util.List;

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

    public enum PollState {
        PRE_OPEN,
        OPEN,
        CLOSED
    }
}