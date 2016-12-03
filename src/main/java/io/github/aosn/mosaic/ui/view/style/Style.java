/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.style;

/**
 * Defines CSS classes.
 * <p>Resource: /webapp/VAADIN/css/mosaic.css</p>
 *
 * @author mikan
 * @since 0.1
 */
public enum Style {
    TITLE,
    SUBTITLE,
    LOGIN_BAR,
    COPYRIGHT,
    POLLS_TABLE,
    CONTENT_PANE,
    HEADING,
    LINK,
    ERROR_LABEL,;
    private final String className;

    Style() {
        this.className = name().toLowerCase().replaceAll("_", "-");
    }

    /**
     * Get name of CSS class.
     *
     * @return name of CSS class
     */
    public String className() {
        return className;
    }
}