/*
 * Copyright (C) 2017-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.notification;

import io.github.aosn.mosaic.domain.model.notification.SlackMessage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;

/**
 * Notify to slack.
 *
 * @author mikan
 * @since 0.5
 */
@Repository
@Slf4j
public class SlackRepository {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Post a message to slack.
     *
     * @param message message
     */
    public void post(String url, SlackMessage message) {
        String json;
        try {
            json = new ObjectMapper().writeValueAsString(message);
        } catch (IOException e) {
            var msg = "Failed to serialize slack message";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        log.info("POST " + url + " params: " + message);
        var client = new OkHttpClient();
        var body = RequestBody.create(JSON, json);
        var request = new Request.Builder().url(url).post(body).build();
        try {
            var response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                log.error("Slack: " + response.code() + " " + response.message());
            }
        } catch (IOException e) {
            var msg = "Failed to send slack message";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
}
