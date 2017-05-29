/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.aosn.mosaic.MosaicApplication;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author mikan
 * @see <a href="https://developer.github.com/v3/orgs/#list-your-organizations">developer.github.com</a>
 * @since 0.5
 */
@Getter
@NoArgsConstructor
public class GitHubOrganization implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @JsonProperty("login")
    private String organization;
}
