/*
 * Copyright (C) 2016-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.poll;

import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.model.issue.GitHubIssue;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Book entity.
 *
 * @author mikan
 * @since 0.1
 */
@Entity
@Table(name = "books")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @Id
    @Column
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    @Getter
    private Long issue;

    @Column(nullable = false)
    @Getter
    private String url;

    @Transient // hand joining via IssueService
    @Getter
    @Setter
    private GitHubIssue gitHubIssue;

    /**
     * Cached number of votes.
     * This field is excluded from database table.
     *
     * @see io.github.aosn.mosaic.domain.service.issue.IssueService#resolveBooks(Poll)
     */
    @Transient
    @Getter
    @Setter
    private int votes;

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
        var otherBook = (Book) other;
        return id.equals(otherBook.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
