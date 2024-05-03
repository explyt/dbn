package com.dbn.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Timers {

    @NotNull
    public static Timer createNamedTimer(@NonNls @NotNull String name, long delay, TimeUnit delayUnit, @NotNull ActionListener listener) {
        int delayMillis = (int) delayUnit.toMillis(delay);
        return new Timer(delayMillis, listener) {
            @Override
            public String toString() {
                return name;
            }
        };
    }

    public static void executeLater(String identifier, int delay, TimeUnit delayUnit, Runnable runnable) {
        Timer timer = createNamedTimer(identifier, delay, delayUnit, e -> runnable.run());
        timer.setRepeats(false);
        timer.start();
    }
}
