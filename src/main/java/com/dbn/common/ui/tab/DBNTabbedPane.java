package com.dbn.common.ui.tab;

import com.dbn.common.ui.util.Listeners;
import com.intellij.ide.ui.laf.darcula.ui.DarculaTabbedPaneUI;
import com.intellij.openapi.Disposable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.function.Function;

public class DBNTabbedPane<T extends Disposable> extends DBNTabbedPaneBase<T> {
    private final Listeners<TabsListener> listeners = new Listeners<>();

    public DBNTabbedPane(Disposable parent) {
        super(parent);

        addChangeListener(e -> {
            DBNTabbedPane source = (DBNTabbedPane) e.getSource();
            int selectedIndex = source.getSelectedIndex();
            listeners.notify(l -> l.selectionChanged(selectedIndex));
        });
        if (false)
        setUI(new DarculaTabbedPaneUI() {
            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                super.paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                super.paintTabBackground(g, tabPlacement, tabIndex, x, y, w, h, isSelected);
            }



            @Override
            protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                super.paintTabArea(g, tabPlacement, selectedIndex);
            }

            @Override
            protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
                super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);

/*
                int selectedIndex = tabPane.getSelectedIndex();
                boolean isSelected = selectedIndex == tabIndex;
                String title = tabPane.getTitleAt(tabIndex);

                Font font = tabPane.getFont();
                FontMetrics metrics = tabPane.getFontMetrics(font);
                paintText(g, tabPlacement, font, metrics, tabIndex, title, textRect, isSelected);
*/
            }


        });
    }



    public void addTabsListener(TabsListener listener) {
        listeners.add(listener);
    }

    public String getSelectedTabTitle() {
        int index = getSelectedIndex();
        return getTitleAt(index);
    }

    public void selectTab(String title) {
        selectTab(title, i -> getTitleAt(i));
    }

    public void selectTab(JComponent component) {
        selectTab(component, i -> getComponentAt(i));
    }

    public void selectTab(T content) {
        selectTab(content, i -> getContentAt(i));
    }

    public void setTabBackground(int index, Color background) {

    }

    private <E> void selectTab(E element, Function<Integer, E> predicate) {
        for (int i = 0; i < getTabCount(); i++) {
            E elementAtIndex = predicate.apply(i);
            if (Objects.equals(elementAtIndex, element)) {
                setSelectedIndex(i);
                return;
            }
        }
    }
}
