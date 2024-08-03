package com.dbn.common.ui.tab;

import com.dbn.common.ui.util.Listeners;
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
    }

    public T getSelectedContent() {
        return getContentAt(getSelectedIndex());
    }

    public T getContentAt(int index) {
        return getTabInfo(index).getContent();
    }

    public Color getTabColor(int index) {
        return getTabInfo(index).getColor();
    }

    public void setTabColor(int index, Color color) {
        getTabInfo(index).setColor(color);
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
