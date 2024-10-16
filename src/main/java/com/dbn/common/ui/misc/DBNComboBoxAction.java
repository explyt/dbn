package com.dbn.common.ui.misc;

import com.dbn.common.action.BackgroundUpdatedAction;
import com.dbn.common.action.ComboBoxAction;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public abstract class DBNComboBoxAction extends ComboBoxAction implements BackgroundUpdatedAction {
    @NotNull
    @Override
    public JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
    JPanel panel=new JPanel(new GridBagLayout());
    ComboBoxButton button = new ComboBoxButton(presentation);
        GridBagConstraints constraints = new GridBagConstraints(
                0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, JBUI.insets(3), 0, 0);
        panel.add(button, constraints);
        panel.setFocusable(false);
        return panel;
    }
}
