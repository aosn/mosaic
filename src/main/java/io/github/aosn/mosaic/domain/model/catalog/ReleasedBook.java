/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.catalog;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.List;

/**
 * @author mikan
 * @since 0.3
 */
public interface ReleasedBook extends Serializable {

    @Nullable
    String getIsbn();

    @Nullable
    String getTitle();

    @Nullable
    String getSubtitle();

    int getPageCount();

    String getPublishedDate();

    @Nullable
    String getPublisher();

    List<String> getAuthors();

    @Nullable
    String getLanguage();

    boolean isEBook();

    @Nullable
    String getThumbnailUrl();
}
