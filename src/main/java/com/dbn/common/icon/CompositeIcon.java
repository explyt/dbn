package com.dbn.common.icon;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CompositeIcon implements Icon {

    private final Icon leftIcon;
    private final Icon rightIcon;
    private final int horizontalStrut;

    public CompositeIcon(@NotNull Icon leftIcon, @NotNull Icon rightIcon, int horizontalStrut) {
        this.leftIcon = leftIcon;
        this.rightIcon = rightIcon;
        this.horizontalStrut = horizontalStrut;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        paintIconAlignedCenter(c, g, x, y, leftIcon);
        paintIconAlignedCenter(c, g, x + leftIcon.getIconWidth() + horizontalStrut, y, rightIcon);
    }

    private void paintIconAlignedCenter(Component c, Graphics g, int x, int y, @NotNull Icon icon) {
        int iconHeight = getIconHeight();
        icon.paintIcon(c, g, x, y + (iconHeight - icon.getIconHeight()) / 2);
    }

    @Override
    public int getIconWidth() {
        return leftIcon.getIconWidth() + horizontalStrut + rightIcon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return Math.max(leftIcon.getIconHeight(), rightIcon.getIconHeight());
    }
}