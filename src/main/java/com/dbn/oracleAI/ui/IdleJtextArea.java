package com.dbn.oracleAI.ui;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.FocusEvent;

/**
 * TextArea class that support been put on hold
 */
@Slf4j
public class IdleJtextArea extends JTextArea {

    // Do this in idle mode?
    private boolean isIdle = false;

    // text to be displayed when we are in idle mode
    private final String idleText;

    /**
     * Creates a new text area
     * @param idleText idle mode text
     */
    public IdleJtextArea(String idleText) {
        super();
        this.idleText = idleText;
        this.isIdle = true;
    }

    /**
     * checks whether this text area is in idle mode
     * @return
     */
    public boolean isIdle () {
        return this.isIdle;
    }

    /**
     * Checks whether this text area is empty, or contains only blanks
     * @return true if empty, false otherwise
     */
    private boolean isEmpty() {
        return (getText() == null || getText().isEmpty());
    }

    /**
     * Turns the idle mode on and off
     * @param idle
     */
    public void setIdleMode(boolean idle) {
        log.debug("setIdleMode, idling="+isIdle);

        if (idle) {
            isIdle = true;
            setText(this.idleText);
            transferFocus();
        } else {
            isIdle = false;
            setText(null);
        }
    }
    @Override
    protected void processFocusEvent(FocusEvent e) {
        log.debug("processFocusEvent, gained/lost="+FocusEvent.FOCUS_GAINED+"/"+FocusEvent.FOCUS_LOST+" id="+e.getID()+", idling="+isIdle);
        if (!isFocusOwner()) {
            if (isEmpty()) {
                // if the text is empty
                // replace by the idling string
                isIdle = true;
                setText(this.idleText);
                log.debug("processFocusEvent, now IDLE ON");
            }
        } else {
            if (isIdle) {
                isIdle = false;
                // let user enter what he wants.
                setText(null);
                log.debug("processFocusEvent, now IDLE OFF");
            }
        }
        super.processFocusEvent(e);
    }
}
