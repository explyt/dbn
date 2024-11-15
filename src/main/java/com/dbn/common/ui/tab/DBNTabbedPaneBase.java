package com.dbn.common.ui.tab;

import com.dbn.common.color.Colors;
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
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.dbn.common.ui.util.UserInterface.findChildComponent;

@Getter
@Setter
@Slf4j
class DBNTabbedPaneBase<T extends Disposable> extends JBTabbedPane implements StatefulDisposable {
    private boolean disposed;
    private final List<DBNTabInfo<T>> tabInfos = new ArrayList<>();

    public DBNTabbedPaneBase(Disposable parent) {
        super(TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        DBNTabbedPaneUI ui = new DBNTabbedPaneUI();
        setUI(ui);

        initHiddenTabsButton(ui);
        Disposer.register(parent, this);
    }

    @Workaround // future api changes could render this code void
    private void initHiddenTabsButton(DBNTabbedPaneUI ui) {
        JButton button = findChildComponent(this, JButton.class, c -> isHiddenTabsButton(c));
        JViewport viewport = findChildComponent(this, JViewport.class, c -> isScrollableViewport(c));
        if (button == null || viewport == null) {
            // if this error is reported, the api has changed and hidden button adjustments are no longer happening
            log.error("Cannot find HiddenTabsButton or ScrollableTabsViewport");
            return;
        }

        button.setBorder(null);
        button.setOpaque(false);
        button.setBackground(Colors.getPanelBackground());

       viewport.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                button.setVisible(hasHiddenTabs(viewport, ui));
                super.componentResized(e);
            }
        });

    }

    private boolean hasHiddenTabs(JViewport viewport, DBNTabbedPaneUI ui) {
        for (int i = 0; i < getTabCount(); i++) {
            Rectangle rectangle = ui.getRectangles()[i];
            if (!viewport.getViewRect().contains(rectangle)) return true;
        }
        return false;
    }

    private static boolean isHiddenTabsButton(JComponent c) {
        return c instanceof JButton && c.getClass().getName().contains("HiddenTabs");
    }

    private static boolean isScrollableViewport(JComponent c) {
        return c instanceof JViewport && Objects.equals(c.getName(), "TabbedPane.scrollableViewport");
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
