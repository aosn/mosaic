/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vaadin.spring.i18n.MessageProvider;
import org.vaadin.spring.i18n.ResourceBundleMessageProvider;
import org.vaadin.spring.i18n.annotation.EnableI18N;

/**
 * @author mikan
 * @since 0.1
 */
@Configuration
@EnableI18N
public class I18nConfig {

    @Bean
    MessageProvider communicationMessages() {
        return new ResourceBundleMessageProvider("messages");
    }
}
