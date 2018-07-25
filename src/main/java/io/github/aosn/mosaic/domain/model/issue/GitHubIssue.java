/*
 * Copyright (C) 2016-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.issue;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.aosn.mosaic.MosaicApplication;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author mikan
 * @see <a href="https://developer.github.com/v3/issues/#list-issues-for-a-repository">developer.github.com</a>
 * @since 0.1
 */
@Getter
@NoArgsConstructor
public class GitHubIssue implements Serializable {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;

    @JsonProperty("number")
    private Long id;

    @JsonProperty("html_url")
    private String url;

    @JsonProperty("state")
    private String state;

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("user")
    private GitHubUser user;

    @JsonProperty("labels")
    private GitHubLabel[] labels;
}
