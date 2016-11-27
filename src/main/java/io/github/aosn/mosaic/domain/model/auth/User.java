/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.auth;

import io.github.aosn.mosaic.MosaicApplication;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author mikan
 * @since 0.1
 */
@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @Id
    @Column
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private Source source;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstLogin;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @Setter
    private Date lastLogin;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        User otherUser = (User) other;
        return id.equals(otherUser.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public enum Source {
        GITHUB
    }
}
