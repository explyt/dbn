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
    private final List<DBNTabInfo<T>> tabInfos = new ArrayList<>();

    public DBNTabbedPaneBase(Disposable parent) {
        //super(TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        setUI(new DBNTabbedPaneUI());

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
        addTabInfo(title, icon, tip, null);
    }

    @Override
    public void addTab(String title, Icon icon, Component component) {
        super.addTab(title, icon, component);
        addTabInfo(title, icon, null, null);
    }

    @Override
    public void addTab(String title, Component component) {
        super.addTab(title, component);
        addTabInfo(title, null, null, null);
    }

    public void addTab(String title, Icon icon, Component component, String tip, T content) {
        addTab(title, icon, component, tip);
        addTabInfo(title, icon, tip, content);
    }

    public void addTab(String title, Icon icon, Component component, T content) {
        super.addTab(title, icon, component);
        addTabInfo(title, icon, null, content);
    }

    public void addTab(String title, Component component, T content) {
        super.addTab(title, component);
        addTabInfo(title, null, null, content);
    }

    private boolean addTabInfo(String title, Icon icon, String tip, T content) {
        return tabInfos.add(new DBNTabInfo<>(title, tip, icon, content));
    }

    public final DBNTabInfo<T> getTabInfo(int index) {
        return tabInfos.get(index);
    }

    @Override
    public void removeTabAt(int index) {
        super.removeTabAt(index);
        DBNTabInfo<T> tabInfo = tabInfos.remove(index);
        Disposer.dispose(tabInfo);
    }

    public void removeAllTabs() {
        while (getTabCount() > 0) {
            removeTabAt(0);
        }

    }

    public void disposeInner() {
        Disposer.dispose(tabInfos);
    }
}
