package com.dbn.common.ui.tab;

import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.StatefulDisposable;
import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBTabbedPane;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DBNTabbedPaneBase<T extends Disposable> extends JBTabbedPane implements StatefulDisposable {
    private boolean disposed;
    private final List<T> contents = new ArrayList<>();

    public DBNTabbedPaneBase(Disposable parent) {
        super(TOP, JTabbedPane.WRAP_TAB_LAYOUT);
        //setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        Disposer.register(parent, this);
    }

    @Override
    public void insertTab(@Nls(capitalization = Nls.Capitalization.Title) String title, Icon icon, Component component, @Nls(capitalization = Nls.Capitalization.Sentence) String tip, int index) {
        super.insertTab(title, icon, component, tip, index);
    }

    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
        super.addTab(title, icon, component, tip);
        contents.add(null);
    }

    @Override
    public void addTab(String title, Icon icon, Component component) {
        super.addTab(title, icon, component);
        contents.add(null);
    }

    @Override
    public void addTab(String title, Component component) {
        super.addTab(title, component);
        contents.add(null);
    }

    public void addTab(String title, Icon icon, Component component, T content, String tip) {
        addTab(title, icon, component, tip);
        contents.add(content);
    }

    public void addTab(String title, Icon icon, Component component, T content) {
        super.addTab(title, icon, component);
        contents.add(content);
    }

    public void addTab(String title, Component component, T content) {
        super.addTab(title, component);
        contents.add(content);
    }

    public T getSelectedContent() {
        return getContentAt(getSelectedIndex());
    }

    public T getContentAt(int index) {
        return contents.get(index);
    }

    @Override
    public void removeTabAt(int index) {
        super.removeTabAt(index);
        T content = contents.remove(index);
        Disposer.dispose(content);
    }

    public void removeAllTabs() {
        while (getTabCount() > 0) {
            removeTabAt(0);
        }

    }

    public void disposeInner() {
        Disposer.dispose(contents);
    }
}
