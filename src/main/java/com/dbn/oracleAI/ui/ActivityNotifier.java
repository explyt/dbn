package com.dbn.oracleAI.ui;

import javax.swing.JProgressBar;

/**
 * A simple subclass of JProgressBar
 * to show endless progression bar
 */
public class ActivityNotifier extends JProgressBar {

    public ActivityNotifier() {
        super(0, 0);
        stop();
    }

    /**
     * Starts to activity.
     * this will realize the progress bar
     */
    public void start() {
        setIndeterminate(true);
        setVisible(true);
    }
    /**
     * Stops to activity.
     * this will hide the progress bar
     */
    public void stop() {
        setMinimum(0);
        setMaximum(0);
        setIndeterminate(false);
        setVisible(false);
    }
}
