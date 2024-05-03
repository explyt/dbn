package com.dbn.common.ui.misc;

import com.dbn.common.thread.Dispatch;
import com.dbn.common.util.Timers;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

public class TemporaryLabel extends JLabel {

    public void show(int timeout, TimeUnit timeoutUnit) {
        changeVisibility(true);
        Timers.executeLater("TemporaryLabelTimeout", timeout, timeoutUnit, () -> changeVisibility(false));
    }

    private void changeVisibility(boolean aFlag) {
        Dispatch.run(() -> setVisible(aFlag));
    }
}
