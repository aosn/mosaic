/*
 * Copyright (C) 2016-2018 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.controller;

import io.github.aosn.mosaic.ui.MainUI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mikan
 * @since 0.1
 */
@RestController
@Slf4j
public class UserController {

    public static final String LOGOUT_PATH = "/logout";

    @GetMapping(value = {"/user", "/me"})
    public Map<String, String> user(Principal principal) {
        log.info("user(" + principal + ")");
        var map = new LinkedHashMap<String, String>();
        map.put("name", principal.getName());
        return map;
    }

    @GetMapping(value = LOGOUT_PATH)
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:" + MainUI.PATH;
    }
}
