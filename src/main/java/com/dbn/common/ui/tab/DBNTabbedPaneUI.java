package com.dbn.common.ui.tab;

import com.dbn.common.color.Colors;
import com.intellij.ide.ui.laf.darcula.ui.DarculaTabbedPaneUI;

import javax.swing.*;
import java.awt.*;

import static com.intellij.util.ui.JBUI.CurrentTheme.TabbedPane.FOCUS_COLOR;

class DBNTabbedPaneUI extends DarculaTabbedPaneUI {
    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
        super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
        DBNTabbedPane<?> tabPane = (DBNTabbedPane<?>) this.tabPane;
        Color color = tabPane.getTabColor(tabIndex);

        if (color == null)
            super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected); else
            paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected, color);

    }

    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected, Color color) {
        // underline
        int hoverTab = 0;
        if (tabPane.isEnabled()) {
            if (tabPane.hasFocus() && isSelected) {
                color = Colors.lafBrighter(color, 2);
            } else if (tabIndex == hoverTab) {
                color = Colors.lafDarker(color, 2);
            }
        }

        g.setColor(color);

        if (tabPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
            if (tabPlacement == LEFT || tabPlacement == RIGHT) {
                w -= 1;//getOffset();
            } else {
                h -= 1;//getOffset();
            }
        }
        g.fillRect(x, y, w, h);
    }


    @Override
    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
        super.paintTabArea(g, tabPlacement, selectedIndex);
    }

    @Override
    protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
        super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
    }


}
