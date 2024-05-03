package com.dbn.common.ui.util;

import com.intellij.ui.JBColor;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import javax.swing.*;
import java.util.Objects;

@UtilityClass
public final class LookAndFeel {
    @Getter
    static boolean darkMode = !JBColor.isBright();

    static {
        UIManager.addPropertyChangeListener(evt -> {
            if (Objects.equals(evt.getPropertyName(), "lookAndFeel")) {
                LookAndFeel.darkMode = !JBColor.isBright();
            }


        });
    }


}
