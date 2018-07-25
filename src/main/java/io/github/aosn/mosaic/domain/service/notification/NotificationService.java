/*
 * Copyright (C) 2016-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.notification;

import io.github.aosn.mosaic.domain.model.notification.SlackMessage;
import io.github.aosn.mosaic.domain.model.poll.Poll;
import io.github.aosn.mosaic.domain.repository.notification.SlackRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author mikan
 * @since 0.1
 */
@Service
@Slf4j
@Async
public class NotificationService {

    private final SlackRepository slackRepository;

    public NotificationService(SlackRepository slackRepository) {
        this.slackRepository = slackRepository;
    }

    public void notifyBeginOfPoll(Poll poll) {
        var group = poll.getGroup();
        if (!group.isSlackEnabled()) {
            return;
        }
        var message = SlackMessage.builder()
                .channel(group.getSlackChannel())
                .username(group.getSlackUsername())
                .text(group.getSlackBeginTemplate().replace("%s", poll.getSubject()))
                .build();
        slackRepository.post(group.getSlackWebhook(), message);
    }

    public void notifyEndOfPoll(Poll poll) {
        var group = poll.getGroup();
        if (!group.isSlackEnabled()) {
            return;
        }
        var message = SlackMessage.builder()
                .channel(group.getSlackChannel())
                .username(group.getSlackUsername())
                .text(group.getSlackEndTemplate().replace("%s", poll.getSubject()))
                .build();
        slackRepository.post(group.getSlackWebhook(), message);
    }
}
