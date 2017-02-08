/*
 * Copyright (C) 2016-2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
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
    USER_ICON,
    USER_COLLECTION,
    ICON_AND_NAME,
    ERROR_LABEL,
    ISSUE_LABEL;
    private final String className;

    Style() {
        // Convert Java to CSS naming convention.
        // e.g. TITLE -> title, CONTENT_PANE -> content-pane
        className = name().toLowerCase().replaceAll("_", "-");
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
