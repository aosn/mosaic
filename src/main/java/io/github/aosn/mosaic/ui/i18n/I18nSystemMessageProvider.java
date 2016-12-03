/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.i18n;

import com.vaadin.server.CustomizedSystemMessages;
import com.vaadin.server.SystemMessages;
import com.vaadin.server.SystemMessagesInfo;
import com.vaadin.server.SystemMessagesProvider;
import io.github.aosn.mosaic.MosaicApplication;
import org.vaadin.spring.i18n.I18N;

/**
 * An internationalized {@link SystemMessagesProvider} implementation.
 *
 * @author mikan
 * @since 0.1
 */
public class I18nSystemMessageProvider implements SystemMessagesProvider {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private final CustomizedSystemMessages messages = new CustomizedSystemMessages();

    public I18nSystemMessageProvider(I18N i18n) {
        messages.setSessionExpiredCaption(i18n.get("system.caption.session.expired"));
        messages.setSessionExpiredMessage(i18n.get("system.description.session.expired"));
        messages.setSessionExpiredNotificationEnabled(true);
        messages.setCommunicationErrorCaption(i18n.get("system.caption.communication.error"));
        messages.setCommunicationErrorMessage(i18n.get("system.description.communication.error"));
        messages.setCommunicationErrorNotificationEnabled(true);
        messages.setAuthenticationErrorCaption(i18n.get("system.caption.authentication.error"));
        messages.setAuthenticationErrorMessage(i18n.get("system.description.authentication.error"));
        messages.setAuthenticationErrorNotificationEnabled(true);
        messages.setInternalErrorCaption(i18n.get("system.caption.internal.error"));
        messages.setInternalErrorMessage(i18n.get("system.description.internal.error"));
        messages.setInternalErrorNotificationEnabled(true);
        messages.setCookiesDisabledCaption(i18n.get("system.caption.cookies.disabled"));
        messages.setCookiesDisabledMessage(i18n.get("system.description.cookies.disabled"));
        messages.setCookiesDisabledNotificationEnabled(true);
    }

    @Override
    public SystemMessages getSystemMessages(SystemMessagesInfo systemMessagesInfo) {
        return messages;
    }
}