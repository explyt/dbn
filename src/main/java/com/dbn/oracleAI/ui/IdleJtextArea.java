package com.dbn.oracleAI.ui;

import com.intellij.openapi.diagnostic.Logger;

import javax.swing.JTextArea;
import java.awt.event.FocusEvent;

/**
 * TextArea class that support been put on hold
 */
public class IdleJtextArea extends JTextArea {
    private static final Logger LOG = Logger.getInstance(IdleJtextArea.class.getPackageName());

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
        LOG.debug("setIdleMode, idling="+isIdle);

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
        LOG.debug("processFocusEvent, gained/lost="+FocusEvent.FOCUS_GAINED+"/"+FocusEvent.FOCUS_LOST+" id="+e.getID()+", idling="+isIdle);
        if (!isFocusOwner()) {
            if (isEmpty()) {
                // if the text is empty
                // replace by the idling string
                isIdle = true;
                setText(this.idleText);
                LOG.debug("processFocusEvent, now IDLE ON");
            }
        } else {
            if (isIdle) {
                isIdle = false;
                // let user enter what he wants.
                setText(null);
                LOG.debug("processFocusEvent, now IDLE OFF");
            }
        }
        super.processFocusEvent(e);
    }
}
