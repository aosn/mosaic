/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.aosn.mosaic.MosaicApplication;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Response of Google Book API, that wraps array of {@link GoogleBook} entities.
 *
 * @author mikan
 * @since 0.3
 */
@SuppressWarnings("unused")
public class GoogleBookResponse implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @JsonProperty
    private String kind;

    @JsonProperty
    private Integer totalItems;

    @JsonProperty
    private GoogleBook[] items;

    public List<GoogleBook> getItems() {
        return Arrays.asList(items);
    }
}
