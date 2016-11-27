/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.service.notification;

import io.github.aosn.mosaic.domain.model.poll.Poll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author mikan
 * @since 0.1
 */
@Service
@Slf4j
public class NotificationService {

    public void notifyCreatePoll(Poll poll) {
        log.info("not implemented yet: NotificationService#notifyCreatePoll");
    }

    public void notifyClosePoll(Poll poll) {
        log.info("not implemented yet: NotificationService#notifyClosePoll");
    }
}
