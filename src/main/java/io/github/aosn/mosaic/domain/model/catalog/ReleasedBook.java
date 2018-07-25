/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.catalog;

import io.github.aosn.mosaic.config.SecurityConfig;
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

    default String getThumbnailOrPlaceholder() {
        var url = getThumbnailUrl();
        return url == null ? SecurityConfig.V_PATH_PREFIX + SecurityConfig.NO_IMAGE_PATH : url;
    }
}
