/*
 * Copyright (C) 2016 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.*;
import io.github.aosn.mosaic.MosaicApplication;
import io.github.aosn.mosaic.domain.service.auth.UserService;
import io.github.aosn.mosaic.domain.service.issue.IssueService;
import io.github.aosn.mosaic.ui.MainUI;
import io.github.aosn.mosaic.ui.view.layout.ContentPane;
import io.github.aosn.mosaic.ui.view.layout.ViewRoot;
import io.github.aosn.mosaic.ui.view.style.Style;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.i18n.I18N;

import java.util.function.Supplier;

/**
 * Error page.
 *
 * @author mikan
 * @since 0.1
 */
@Slf4j
@SpringView(name = ErrorView.VIEW_NAME)
public class ErrorView extends CustomComponent implements View {

    public static final String VIEW_NAME = "error";
    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private static final String ATTR_ERROR_MESSAGE = "mosaic.ui.error.message";
    private static final String ATTR_ERROR_THROWABLE = "mosaic.ui.error.throwable";
    private transient final I18N i18n;
    private transient final UserService userService;

    @Autowired
    public ErrorView(I18N i18n, UserService userService) {
        this.i18n = i18n;
        this.userService = userService;
    }

    static void show(String message, Throwable throwable) {
        UI ui = UI.getCurrent();
        VaadinSession session = ui.getSession();
        session.setAttribute(ATTR_ERROR_MESSAGE, message);
        session.setAttribute(ATTR_ERROR_THROWABLE, throwable);
        ui.getNavigator().navigateTo(VIEW_NAME);
    }

    /**
     * Show {@link ErrorView} if function throws exception
     *
     * @param function supplier function
     * @return supplied result or {@code null} if get failed
     */
    @Nullable
    static <T> T showIfExceptionThrows(Supplier<T> function) {
        try {
            return function.get();
        } catch (IssueService.IssueAccessException e) {
            ErrorView.show("API Error", e);
        } catch (RuntimeException e) {
            ErrorView.show("DB Error", e);
        } catch (Throwable e) {
            ErrorView.show("Unexpected Error", e);
        }
        return null;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        setCompositionRoot(new ViewRoot(i18n, userService, createErrorLayout()));
    }

    private Layout createErrorLayout() {
        ContentPane contentPane = new ContentPane();

        VaadinSession session = VaadinSession.getCurrent();
        String message = (String) session.getAttribute(ATTR_ERROR_MESSAGE);
        Throwable throwable = (Throwable) session.getAttribute(ATTR_ERROR_THROWABLE);

        // ERROR label
        Label errorLabel = new Label("ERROR");
        errorLabel.setStyleName(Style.ERROR_LABEL.className());
        contentPane.addComponent(errorLabel);

        // Messages
        if (message != null) {
            contentPane.addComponent(new Label(message));
        }
        if (throwable != null) {
            contentPane.addComponent(new Label(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()));
            log.error("Received error: " + throwable.getMessage(), throwable);
        }

        // Back button
        Button backButton = new Button(i18n.get("common.button.back"),
                e -> getUI().getPage().setLocation(MainUI.PATH));
        contentPane.addComponent(backButton);
        contentPane.setComponentAlignment(backButton, Alignment.MIDDLE_CENTER);

        return contentPane;
    }
}
