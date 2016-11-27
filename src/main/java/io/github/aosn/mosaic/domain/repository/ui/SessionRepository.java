/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.domain.repository.ui;

import com.vaadin.server.VaadinSession;
import io.github.aosn.mosaic.domain.model.auth.User;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Repository;

/**
 * Repository of user session.
 *
 * @author mikan
 * @see VaadinSession
 * @since 0.1
 */
@Repository
@Slf4j
public class SessionRepository {

    @Nullable
    public User getUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session == null) {
            return null;
        }
        User user = session.getAttribute(User.class);
        log.debug("Session: GET User(" + (user == null ? "null" : user.getName()) + ")");
        return user;
    }

    public void setUser(@Nullable User user) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(User.class, user);
            log.debug("Session: SET User(" + (user == null ? "null" : user.getName()) + ")");
        }
    }

    public void clearUser() {
        setUser(null);
    }
}
