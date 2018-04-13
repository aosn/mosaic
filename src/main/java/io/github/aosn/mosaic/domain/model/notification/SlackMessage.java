/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.model.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@JsonRootName("payload")
@Builder
@Getter
@ToString
public class SlackMessage {

    private String channel;

    private String username;

    private String text;

    @JsonProperty("icon_emoji")
    private String iconEmoji;
}
