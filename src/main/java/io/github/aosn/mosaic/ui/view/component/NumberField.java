/*
 * Copyright (C) 2017 Alice on Sunday Nights Workshop Participants. All rights reserved.
 */
package io.github.aosn.mosaic.ui.view.component;

import com.vaadin.event.FieldEvents;
import com.vaadin.ui.TextField;
import io.github.aosn.mosaic.MosaicApplication;

/**
 * Number only {@link TextField}.
 *
 * @author mikan
 * @see <a href="http://stackoverflow.com/questions/17144798">StackOverflow #17144798</a>
 * @since 0.3
 */
public class NumberField extends TextField implements FieldEvents.TextChangeListener {

    private static final long serialVersionUID = MosaicApplication.MOSAIC_SERIAL_VERSION_UID;
    private final int initialValue;
    private String lastValue;

    public NumberField(String caption, int value) {
        super(caption, Integer.toString(value));
        setImmediate(true);
        setTextChangeEventMode(TextChangeEventMode.EAGER);
        addTextChangeListener(this);
        initialValue = value;
        lastValue = getValue();
    }

    public int getValueAsInt() {
        try {
            return new Integer(lastValue);
        } catch (NumberFormatException e) {
            return initialValue;
        }
    }

    @Override
    public void textChange(FieldEvents.TextChangeEvent event) {
        String text = event.getText();
        try {
            new Integer(text);
            lastValue = text;
        } catch (NumberFormatException e) {
            setValue(lastValue);
        }
    }
}