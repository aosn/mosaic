/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.poll;

import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.auth.User;
import lombok.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Group provides multiple repository support.
 *
 * @author mikan
 * @since 0.2
 */
@Entity
@Table(name = "groups")
@EqualsAndHashCode(exclude = "owner")
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Group implements Serializable {

    public static final String LABEL_FILTER_PATTERN = "%s";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    public Group(String organization, String repository, String labelFilter, @Nullable User owner) {
        this.groupKey = new GroupKey(organization, repository);
        this.labelFilter = labelFilter;
        this.owner = owner;
    }

    @EmbeddedId
    private GroupKey groupKey;

    @Column(nullable = false, length = 64)
    @Getter
    private String labelFilter;

    @JoinColumn
    @OneToOne
    @Nullable
    @Getter
    private User owner; // null for default, otherwise for custom record

    public String getOrganization() {
        return groupKey.organization;
    }

    public String getRepository() {
        return groupKey.repository;
    }

    public void replace(Group group) {
        groupKey.organization = group.getOrganization();
        groupKey.repository = group.getRepository();
        labelFilter = group.labelFilter;
    }

    @Embeddable
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupKey implements Serializable {

        private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

        @Column(nullable = false, length = 40)
        private String organization;

        @Column(nullable = false, length = 100)
        private String repository;

        @Override
        public String toString() {
            return String.format("GroupKey(org=%s, repo=%s)", organization, repository);
        }
    }
}
