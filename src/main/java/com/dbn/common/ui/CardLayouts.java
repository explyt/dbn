package com.dbn.common.ui;

import com.dbn.common.ui.form.DBNForm;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

@UtilityClass
public class CardLayouts {

    private static final String BLANK_PANEL_ID = "BLANK_PANEL";

    public static void addBlankCard(JPanel container) {
        addBlankCard(container, -1, -1);
    }

    public static void addBlankCard(JPanel container, int width, int height) {
        JPanel blankPanel = new JPanel();
        blankPanel.setPreferredSize(new Dimension(width, height));
        container.add(blankPanel, BLANK_PANEL_ID);
    }

    public static void addCard(JPanel container, JComponent component, Object identifier) {
        container.add(component, Objects.toString(identifier));
    }

    public static void addCard(JPanel container, DBNForm component, Object identifier) {
        addCard(container, component.getComponent(), identifier);
    }

    public static void showCard(JPanel container, @Nullable Object identifier) {
        CardLayout cardLayout = (CardLayout) container.getLayout();
        cardLayout.show(container, Objects.toString(identifier));
    }

    public static void showBlankCard(JPanel container) {
        showCard(container, BLANK_PANEL_ID);
    }
}
