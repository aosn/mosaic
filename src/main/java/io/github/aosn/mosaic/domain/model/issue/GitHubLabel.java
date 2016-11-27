/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.issue;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.aosn.mosaic.MosaicApplication;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author mikan
 * @since 0,1
 */
@Getter
@NoArgsConstructor
public class GitHubLabel implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;


    @JsonProperty("id")
    private long id;

    @JsonProperty("url")
    private String url;

    @JsonProperty("name")
    private String name;

    @JsonProperty("color")
    private String color;
}
