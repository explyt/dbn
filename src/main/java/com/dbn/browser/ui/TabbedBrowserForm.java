package com.dbn.browser.ui;

import com.dbn.browser.model.BrowserTreeEventListener;
import com.dbn.browser.model.BrowserTreeNode;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.environment.EnvironmentType;
import com.dbn.common.environment.options.EnvironmentSettings;
import com.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.ui.tab.DBNTabInfo;
import com.dbn.common.ui.tab.DBNTabbedPane;
import com.dbn.common.ui.tab.TabsListener;
import com.dbn.common.util.Commons;
import com.dbn.connection.ConnectionBundle;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.ConnectionManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import static com.dbn.common.dispose.Failsafe.guarded;

public class TabbedBrowserForm extends DatabaseBrowserForm{
    private final DBNTabbedPane<SimpleBrowserForm> connectionTabs;
    private JPanel mainPanel;

    TabbedBrowserForm(@NotNull BrowserToolWindowForm parent) {
        super(parent);
        connectionTabs = new DBNTabbedPane<>(this);
        connectionTabs.setAutoscrolls(true);
        //mainPanel.add(connectionTabs, BorderLayout.CENTER);
        initBrowserForms();
        ProjectEvents.subscribe(ensureProject(), this, EnvironmentManagerListener.TOPIC, environmentManagerListener());
        connectionTabs.addTabsListener(createTabsListener());
    }

    private @NotNull TabsListener createTabsListener() {
        return selectedIndex -> ProjectEvents.notify(ensureProject(),
                BrowserTreeEventListener.TOPIC,
                (listener) -> listener.selectionChanged());
    }

    @NotNull
    private EnvironmentManagerListener environmentManagerListener() {
        return new EnvironmentManagerListener() {
            @Override
            public void configurationChanged(Project project) {
                EnvironmentSettings environmentSettings = getEnvironmentSettings(project);
                EnvironmentVisibilitySettings visibilitySettings = environmentSettings.getVisibilitySettings();
                for (DBNTabInfo tabInfo : getTabInfos()) {
                    guarded(tabInfo, ti -> updateTabColor(ti, visibilitySettings));
                }
            }
        };
    }

    private static void updateTabColor(DBNTabInfo tabInfo, EnvironmentVisibilitySettings visibilitySettings) {
        SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getContent();
        ConnectionHandler connection = browserForm.getConnection();
        if (connection == null) return;

        if (visibilitySettings.getConnectionTabs().value()) {
            Color environmentColor = connection.getEnvironmentType().getColor();
            tabInfo.setColor(environmentColor);
        } else {
            tabInfo.setColor(null);
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
            String title = Commons.nvl(connection.getName(), txt("app.connection.placeholder.UnnamedConnection"));
            Icon icon = null; //connection.getIcon();

            DBNTabInfo tabInfo = this.connectionTabs.addTab(title, icon, component, browserForm);

            EnvironmentType environmentType = connection.getEnvironmentType();
            tabInfo.setColor(environmentType.getColor());
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
        var connectionTabs = getConnectionTabs();
        for (var tabInfo : connectionTabs.getTabInfos()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getContent();
            ConnectionHandler connection = browserForm.getConnection();
            if (connection != null && connection.getConnectionId() == connectionId) {
                return browserForm;
            }
        }
        return null;
    }

    @Nullable
    private SimpleBrowserForm removeBrowserForm(ConnectionId connectionId) {
        var connectionTabs = getConnectionTabs();
        for (var tabInfo : connectionTabs.getTabInfos()) {
            SimpleBrowserForm browserForm = tabInfo.getContent();
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
        return browserForm == null ? null : browserForm.getBrowserTree();
    }

    @Nullable
    private SimpleBrowserForm getSelectedBrowserForm() {
        DBNTabInfo<SimpleBrowserForm> tabInfo = getSelectedTabInfo();
        return tabInfo == null ? null : tabInfo.getContent();
    }

    private DBNTabInfo<SimpleBrowserForm> getSelectedTabInfo() {
        return getConnectionTabs().getSelectedTabInfo();
    }

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        SimpleBrowserForm browserForm = getSelectedBrowserForm();
        return browserForm == null ? null : browserForm.getBrowserTree();
    }

    @Override
    public ConnectionId getSelectedConnection() {
        SimpleBrowserForm browserForm = getSelectedBrowserForm();
        return browserForm == null ? null : browserForm.getConnectionId();
    }

    @Override
    public void selectConnection(ConnectionId connectionId) {
        SimpleBrowserForm browserForm = getBrowserForm(connectionId);
        if (browserForm == null) return;
        getConnectionTabs().selectTab(browserForm);
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
        getTabInfos()
            .stream()
            .map(ti -> (SimpleBrowserForm) ti.getContent())
            .forEach(f -> f.rebuildTree());
    }

    @NotNull
    public DBNTabbedPane<SimpleBrowserForm> getConnectionTabs() {
        return Failsafe.nn(connectionTabs);
    }

    void refreshTabInfo(ConnectionId connectionId) {
        for (DBNTabInfo tabInfo : getTabInfos()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getContent();
            ConnectionHandler connection = browserForm.getConnection();
            if (connection == null) continue;

            if (connection.getConnectionId() == connectionId) {
                tabInfo.setTitle(connection.getName());
                break;
            }
        }

    }

    private List<DBNTabInfo<SimpleBrowserForm>> getTabInfos() {
        return getConnectionTabs().getTabInfos();
    }
}

