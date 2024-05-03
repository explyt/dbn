package com.dbn.connection.config.ui;

import com.dbn.common.color.Colors;
import com.dbn.common.environment.EnvironmentType;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.icon.Icons;
import com.dbn.common.options.ConfigurationHandle;
import com.dbn.common.options.SettingsChangeNotifier;
import com.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.common.ui.tab.TabbedPane;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.common.util.Messages;
import com.dbn.common.util.Safe;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.ConnectionManager;
import com.dbn.connection.ConnectivityStatus;
import com.dbn.connection.DatabaseType;
import com.dbn.connection.config.*;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public class ConnectionSettingsForm extends CompositeConfigurationEditorForm<ConnectionSettings> {
    private JPanel mainPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private JButton infoButton;
    private JButton testButton;
    private JCheckBox activeCheckBox;

    private final TabbedPane tabbedPane;
    private final DBNHeaderForm headerForm;

    public ConnectionSettingsForm(ConnectionSettings connectionSettings) {
        super(connectionSettings);
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        tabbedPane = new TabbedPane(this);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);


        TabInfo connectionTabInfo = new TabInfo(new JBScrollPane(databaseSettings.createComponent()));
        connectionTabInfo.setText("Database");
        tabbedPane.addTab(connectionTabInfo);

        if (databaseSettings.getConfigType() == ConnectionConfigType.BASIC) {
            ConnectionSslSettings sslSettings = connectionSettings.getSslSettings();
            TabInfo sslTabInfo = new TabInfo(new JBScrollPane(sslSettings.createComponent()));
            sslTabInfo.setText("SSL");
            tabbedPane.addTab(sslTabInfo);

            ConnectionSshTunnelSettings sshTunnelSettings = connectionSettings.getSshTunnelSettings();
            TabInfo sshTunnelTabInfo = new TabInfo(new JBScrollPane(sshTunnelSettings.createComponent()));
            sshTunnelTabInfo.setText("SSH Tunnel");
            tabbedPane.addTab(sshTunnelTabInfo);
        }

        ConnectionPropertiesSettings propertiesSettings = connectionSettings.getPropertiesSettings();
        TabInfo propertiesTabInfo = new TabInfo(new JBScrollPane(propertiesSettings.createComponent()));
        propertiesTabInfo.setText("Properties");
        tabbedPane.addTab(propertiesTabInfo);

        ConnectionDetailSettings detailSettings = connectionSettings.getDetailSettings();
        TabInfo detailsTabInfo = new TabInfo(new JBScrollPane(detailSettings.createComponent()));
        detailsTabInfo.setText("Details");
        tabbedPane.addTab(detailsTabInfo);

        if (databaseSettings.getDatabaseType() == DatabaseType.ORACLE) {
            ConnectionDebuggerSettings debuggerSettings = connectionSettings.getDebuggerSettings();
            TabInfo debuggerTabInfo = new TabInfo(new JBScrollPane(debuggerSettings.createComponent()));
            debuggerTabInfo.setText("Debugger");
            tabbedPane.addTab(debuggerTabInfo);
        }

        ConnectionFilterSettings filterSettings = connectionSettings.getFilterSettings();
        TabInfo filtersTabInfo = new TabInfo(new JBScrollPane(filterSettings.createComponent()));
        filtersTabInfo.setText("Filters");
        tabbedPane.addTab(filtersTabInfo);

        ConnectivityStatus connectivityStatus = databaseSettings.getConnectivityStatus();
        Icon icon = connectionSettings.isNew() ? Icons.CONNECTION_NEW :
                   !connectionSettings.isActive() ? Icons.CONNECTION_DISABLED :
                   connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_CONNECTED :
                   connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;

        String name = connectionSettings.getDatabaseSettings().getName();
        JBColor color = detailSettings.getEnvironmentType().getColor();

        headerForm = new DBNHeaderForm(this, name, icon, color);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        ProjectEvents.subscribe(ensureProject(), this, ConnectionPresentationChangeListener.TOPIC, connectionPresentationChangeListener);

        //databaseSettingsForm.notifyPresentationChanges();
        //detailSettingsForm.notifyPresentationChanges();

        resetFormChanges();

        registerComponent(testButton);
        registerComponent(infoButton);
        registerComponent(activeCheckBox);
    }

    public ConnectionSettings getTemporaryConfig() throws ConfigurationException {
        try {
            ConfigurationHandle.setTransitory(true);

            UserInterface.stopTableCellEditing(mainPanel);
            ConnectionSettings configuration = getConfiguration();
            ConnectionSettings clone = configuration.clone();
            clone.getDatabaseSettings().getAuthenticationInfo().setTemporary(true);

            ConnectionDatabaseSettingsForm databaseSettingsEditor = configuration.getDatabaseSettings().getSettingsEditor();
            if(databaseSettingsEditor != null) databaseSettingsEditor.applyFormChanges(clone.getDatabaseSettings());

            ConnectionPropertiesSettingsForm propertiesSettingsEditor = configuration.getPropertiesSettings().getSettingsEditor();
            if (propertiesSettingsEditor != null) propertiesSettingsEditor.applyFormChanges(clone.getPropertiesSettings());

            ConnectionSshTunnelSettingsForm sshTunnelSettingsForm = configuration.getSshTunnelSettings().getSettingsEditor();
            if (sshTunnelSettingsForm != null) sshTunnelSettingsForm.applyFormChanges(clone.getSshTunnelSettings());

            ConnectionSslSettingsForm sslSettingsForm = configuration.getSslSettings().getSettingsEditor();
            if (sslSettingsForm != null) sslSettingsForm.applyFormChanges(clone.getSslSettings());

            ConnectionDetailSettingsForm detailSettingsForm = configuration.getDetailSettings().getSettingsEditor();
            if (detailSettingsForm != null) detailSettingsForm.applyFormChanges(clone.getDetailSettings());

            ConnectionDebuggerSettingsForm debuggerSettingsForm = configuration.getDebuggerSettings().getSettingsEditor();
            if (debuggerSettingsForm != null) debuggerSettingsForm.applyFormChanges(clone.getDebuggerSettings());

            ConnectionFilterSettingsForm filterSettingsForm = configuration.getFilterSettings().getSettingsEditor();
            if (filterSettingsForm != null) filterSettingsForm.applyFormChanges(clone.getFilterSettings());

            return clone;
        } finally {
            ConfigurationHandle.setTransitory(false);
        }
    }

    @Override
    protected ActionListener createActionListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                ConnectionSettings configuration = getConfiguration();
                if (source == testButton || source == infoButton) {
                    ConnectionSettingsForm connectionSettingsForm = configuration.getSettingsEditor();
                    if (connectionSettingsForm != null) {
                        Project project = ensureProject();
                        try {
                            ConnectionSettings temporaryConfig = connectionSettingsForm.getTemporaryConfig();
                            ConnectionManager connectionManager = ConnectionManager.getInstance(project);

                            if (source == testButton) connectionManager.testConfigConnection(temporaryConfig, true);
                            if (source == infoButton) {
                                ConnectionDetailSettingsForm detailSettingsForm = configuration.getDetailSettings().getSettingsEditor();
                                if (detailSettingsForm != null) {
                                    EnvironmentType environmentType = detailSettingsForm.getSelectedEnvironmentType();
                                    connectionManager.showConnectionInfo(temporaryConfig, environmentType);
                                }
                            }
                            configuration.getDatabaseSettings().setConnectivityStatus(temporaryConfig.getDatabaseSettings().getConnectivityStatus());

                            refreshConnectionList(configuration);
                        } catch (ConfigurationException e1) {
                            conditionallyLog(e1);
                            Messages.showErrorDialog(project, "Configuration error", e1.getMessage());
                        }
                    }
                }
                if (source == activeCheckBox) {
                    configuration.setModified(true);
                    refreshConnectionList(configuration);
                }

            }

            private void refreshConnectionList(ConnectionSettings configuration) {
                ConnectionBundleSettings bundleSettings = configuration.getParent();
                ConnectionBundleSettingsForm bundleSettingsEditor = bundleSettings.getSettingsEditor();
                if (bundleSettingsEditor == null) return;

                JList connectionList = bundleSettingsEditor.getList();
                UserInterface.repaint(connectionList);
                ConnectionDatabaseSettingsForm settingsEditor = configuration.getDatabaseSettings().getSettingsEditor();
                if (settingsEditor == null) return;

                settingsEditor.notifyPresentationChanges();
            }
        };
    }

    public boolean isConnectionActive() {
        return activeCheckBox.isSelected();
    }

    public void selectTab(String tabName) {
        Safe.run(tabbedPane, t -> t.selectTab(tabName));
    }

    public String getSelectedTabName() {
        return Safe.call(tabbedPane, t -> t.getSelectedTabName());
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    private final ConnectionPresentationChangeListener connectionPresentationChangeListener = new ConnectionPresentationChangeListener() {
        @Override
        public void presentationChanged(String name, Icon icon, Color color, ConnectionId connectionId, DatabaseType databaseType) {
            Dispatch.run(() -> {
                if (isNotValid(ConnectionSettingsForm.this)) return;

                ConnectionSettings configuration = getConfiguration();
                if (!configuration.getConnectionId().equals(connectionId)) return;

                DBNHeaderForm header = headerForm;
                if (header == null) return;

                if (name != null) header.setTitle(name);
                if (icon != null) header.setIcon(icon);
                if (color != null) header.setBackground(color); else header.setBackground(Colors.getPanelBackground());
                //if (databaseType != null) databaseIconLabel.setIcon(databaseType.getLargeIcon());
            });
        }
    };

    @Override
    public void resetFormChanges() {
        activeCheckBox.setSelected(getConfiguration().isActive());
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        UserInterface.stopTableCellEditing(mainPanel);
        applyFormChanges(getConfiguration());
    }

    @Override
    public void applyFormChanges(ConnectionSettings configuration) throws ConfigurationException {
        boolean settingsChanged = configuration.isActive() != activeCheckBox.isSelected();
        configuration.setActive(activeCheckBox.isSelected());

        SettingsChangeNotifier.register(() -> {
            if (settingsChanged) {
                ProjectEvents.notify(getProject(),
                        ConnectionConfigListener.TOPIC,
                        listener -> listener.connectionChanged(configuration.getConnectionId()));
            }
        });
    }
}
