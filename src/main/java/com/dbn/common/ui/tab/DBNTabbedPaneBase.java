package com.dbn.common.ui.tab;

import com.dbn.common.Wrapper;
import com.dbn.common.compatibility.Workaround;
import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.StatefulDisposable;
import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBTabbedPane;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nls;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import java.awt.Component;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import static com.dbn.common.util.Unsafe.cast;

@Getter
@Setter
@Slf4j
class DBNTabbedPaneBase<T extends Disposable> extends JBTabbedPane implements StatefulDisposable {
    private boolean disposed;
    private final List<DBNTabInfo<T>> tabInfos = new ArrayList<>();

    public DBNTabbedPaneBase(Disposable parent) {
        super(TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        setUI(new DBNTabbedPaneUI());

        Disposer.register(parent, this);
    }

    @Override
    public void updateUI() {
        setUI(new DBNTabbedPaneUI());
    }

    @Workaround // see assumption in BasicTabbedPaneUI.scrollableTabLayoutEnabled()
    public LayoutManager getLayout() {
        LayoutManager layout = super.getLayout();
        if (layout instanceof Wrapper) {
            Wrapper wrapped = (Wrapper) layout;
            return cast(wrapped.unwrap());
        }
        return layout;
    }


    @Override
    public void insertTab(@Nls(capitalization = Nls.Capitalization.Title) String title, Icon icon, Component component, @Nls(capitalization = Nls.Capitalization.Sentence) String tip, int index) {
        super.insertTab(title, icon, component, tip, index);
        setTabComponentAt(index, null);
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

    public DBNTabInfo addTab(String title, Icon icon, Component component, String tip, T content) {
        addTab(title, icon, component, tip);
        return addTabInfo(title, icon, tip, content);
    }

    public DBNTabInfo addTab(String title, Icon icon, Component component, T content) {
        super.addTab(title, icon, component);
        return addTabInfo(title, icon, null, content);
    }

    public DBNTabInfo addTab(String title, Component component, T content) {
        super.addTab(title, component);
        return addTabInfo(title, null, null, content);
    }

    private DBNTabInfo addTabInfo(String title, Icon icon, String tip, T content) {
        DBNTabInfo<T> tabInfo = new DBNTabInfo<>(title, tip, icon, content);
        tabInfos.add(tabInfo);
        return tabInfo;
    }

    public final DBNTabInfo<T> getSelectedTabInfo() {
        int selectedIndex = getSelectedIndex();
        return getTabInfo(selectedIndex);
    }

    public final DBNTabInfo<T> getTabInfo(int index) {
        return tabInfos.get(index);
    }

    public void removeTab(DBNTabInfo tabInfo, boolean disposeContent) {
        int index = tabInfos.indexOf(tabInfo);
        super.removeTabAt(index);
        tabInfo = tabInfos.remove(index);

        if (disposeContent) Disposer.dispose(tabInfo);
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
