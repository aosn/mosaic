/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.poll;

import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Vote entity.
 *
 * @author mikan
 * @since 0.1
 */
@Entity
@Table(name = "votes")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Vote implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @Id
    @Column
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    private Date date;

    @JoinColumn(nullable = false)
    @ManyToOne
    @Getter
    private User user;

    @JoinColumn(nullable = false)
    @ManyToOne
    @Getter
    private Book book;

    @JoinColumn(nullable = false)
    @ManyToOne
    private Poll poll;
}
