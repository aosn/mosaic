/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
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
@Getter
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
    private Long issue;

    @Column(nullable = false)
    private String url;

    @Transient
    @Setter
    private GitHubIssue gitHubIssue;

    @Transient
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
        Book otherBook = (Book) other;
        return id.equals(otherBook.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
