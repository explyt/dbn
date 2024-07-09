package com.dbn.browser.ui;

import com.dbn.browser.model.BrowserTreeEventListener;
import com.dbn.browser.model.BrowserTreeNode;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.environment.EnvironmentType;
import com.dbn.common.environment.options.EnvironmentSettings;
import com.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.ui.tab.DBNColoredTabs;
import com.dbn.common.util.Commons;
import com.dbn.connection.ConnectionBundle;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.ConnectionManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.dbn.common.dispose.Failsafe.guarded;

public class TabbedBrowserForm extends DatabaseBrowserForm{
    private final DBNColoredTabs connectionTabs;
    private JPanel mainPanel;

    TabbedBrowserForm(@NotNull BrowserToolWindowForm parent) {
        super(parent);
        connectionTabs = new DBNColoredTabs(this);
        //connectionTabs.setSingleRow(false);
        connectionTabs.setHideTabs(false);
        connectionTabs.setAutoscrolls(true);
        //connectionTabs.setBackground(GUIUtil.getListBackground());
        //mainPanel.add(connectionTabs, BorderLayout.CENTER);
        initBrowserForms();
        ProjectEvents.subscribe(ensureProject(), this, EnvironmentManagerListener.TOPIC, environmentManagerListener());
        connectionTabs.addListener(createTabsListener());
    }

    private @NotNull TabsListener createTabsListener() {
        return new TabsListener() {
            @Override
            public void selectionChanged(@Nullable TabInfo oldSelection, @Nullable TabInfo newSelection) {
                ProjectEvents.notify(ensureProject(),
                        BrowserTreeEventListener.TOPIC,
                        (listener) -> listener.selectionChanged());
            }
        };
    }

    @NotNull
    private EnvironmentManagerListener environmentManagerListener() {
        return new EnvironmentManagerListener() {
            @Override
            public void configurationChanged(Project project) {
                EnvironmentSettings environmentSettings = getEnvironmentSettings(project);
                EnvironmentVisibilitySettings visibilitySettings = environmentSettings.getVisibilitySettings();
                for (TabInfo tabInfo : listTabs()) {
                    guarded(tabInfo, ti -> updateTabColor(ti, visibilitySettings));
                }
            }
        };
    }

    private static void updateTabColor(TabInfo tabInfo, EnvironmentVisibilitySettings visibilitySettings) {
        SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
        ConnectionHandler connection = browserForm.getConnection();
        if (connection == null) return;

        if (visibilitySettings.getConnectionTabs().value()) {
            Color environmentColor = connection.getEnvironmentType().getColor();
            tabInfo.setTabColor(environmentColor);
        } else {
            tabInfo.setTabColor(null);
        }
    }


    private void initBrowserForms() {
        JPanel mainPanel = this.mainPanel;
        if (mainPanel == null) return;

        Project project = ensureProject();
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        for (ConnectionHandler connection: connectionBundle.getConnections()) {
            SimpleBrowserForm browserForm = new SimpleBrowserForm(this, connection);

            JComponent component = browserForm.getComponent();
            TabInfo tabInfo = new TabInfo(component);
            tabInfo.setText(Commons.nvl(connection.getName(), txt("app.connection.placeholder.UnnamedConnection")));
            tabInfo.setObject(browserForm);
            //tabInfo.setIcon(connection.getIcon());
            this.connectionTabs.addTab(tabInfo);

            EnvironmentType environmentType = connection.getEnvironmentType();
            tabInfo.setTabColor(environmentType.getColor());
        }
        if (this.connectionTabs.getTabCount() == 0) {
            mainPanel.removeAll();
            mainPanel.add(new JBList(new ArrayList()), BorderLayout.CENTER);
        } else {
            if (mainPanel.getComponentCount() > 0) {
                Component component = mainPanel.getComponent(0);
                if (component != this.connectionTabs) {
                    mainPanel.removeAll();
                    mainPanel.add(this.connectionTabs, BorderLayout.CENTER);
                }
            } else {
                mainPanel.add(this.connectionTabs, BorderLayout.CENTER);
            }
        }
    }

    @Nullable
    private SimpleBrowserForm getBrowserForm(ConnectionId connectionId) {
        for (TabInfo tabInfo : listTabs()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            ConnectionHandler connection = browserForm.getConnection();
            if (connection != null && connection.getConnectionId() == connectionId) {
                return browserForm;
            }
        }
        return null;
    }

    @Nullable
    private SimpleBrowserForm removeBrowserForm(ConnectionId connectionId) {
        DBNColoredTabs connectionTabs = getConnectionTabs();
        for (TabInfo tabInfo : connectionTabs.getTabs()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            ConnectionId tabConnectionId = browserForm.getConnectionId();
            if (tabConnectionId == connectionId) {
                connectionTabs.removeTab(tabInfo, false);
                return browserForm;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    @Nullable
    public DatabaseBrowserTree getBrowserTree() {
        return getActiveBrowserTree();
    }

    @Nullable
    public DatabaseBrowserTree getBrowserTree(ConnectionId connectionId) {
        SimpleBrowserForm browserForm = getBrowserForm(connectionId);
        return browserForm== null ? null : browserForm.getBrowserTree();
    }

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        TabInfo tabInfo = getConnectionTabs().getSelectedInfo();
        if (tabInfo == null) return null;

        SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
        return browserForm.getBrowserTree();
    }

    @Override
    public ConnectionId getSelectedConnection() {
        TabInfo tabInfo = getConnectionTabs().getSelectedInfo();
        if (tabInfo == null) return null;

        SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
        return browserForm.getConnectionId();
    }

    @Override
    public void selectConnection(ConnectionId connectionId) {
        SimpleBrowserForm browserForm = getBrowserForm(connectionId);
        if (browserForm == null) return;

        getConnectionTabs().select(browserForm.getComponent(), true);
    }

    @Override
    public void selectElement(BrowserTreeNode treeNode, boolean focus, boolean scroll) {
        ConnectionId connectionId = treeNode.getConnectionId();
        SimpleBrowserForm browserForm = getBrowserForm(connectionId);
        if (browserForm == null) return;

        if (scroll) browserForm.selectElement(treeNode, focus, true);

        selectConnection(connectionId);
    }

    @Override
    public void rebuildTree() {
        listTabs()
            .stream()
            .map(ti -> (SimpleBrowserForm) ti.getObject())
            .forEach(f -> f.rebuildTree());
    }

    @NotNull
    public DBNColoredTabs getConnectionTabs() {
        return Failsafe.nn(connectionTabs);
    }

    void refreshTabInfo(ConnectionId connectionId) {
        for (TabInfo tabInfo : listTabs()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            ConnectionHandler connection = browserForm.getConnection();
            if (connection == null) continue;

            if (connection.getConnectionId() == connectionId) {
                tabInfo.setText(connection.getName());
                break;
            }
        }

    }

    @NotNull
    private List<TabInfo> listTabs() {
        return new ArrayList<>(getConnectionTabs().getTabs());
    }
}

