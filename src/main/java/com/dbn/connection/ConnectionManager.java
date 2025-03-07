/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.connection;

import com.dbn.DatabaseNavigator;
import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.component.ProjectManagerListener;
import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.database.DatabaseInfo;
import com.dbn.common.dispose.Disposer;
import com.dbn.common.environment.EnvironmentType;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.message.MessageCallback;
import com.dbn.common.routine.Consumer;
import com.dbn.common.thread.Background;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.thread.Progress;
import com.dbn.common.util.Dialogs;
import com.dbn.common.util.Editors;
import com.dbn.common.util.Lists;
import com.dbn.common.util.Strings;
import com.dbn.common.util.TimeUtil;
import com.dbn.connection.config.ConnectionConfigListener;
import com.dbn.connection.config.ConnectionDatabaseSettings;
import com.dbn.connection.config.ConnectionSettings;
import com.dbn.connection.config.file.DatabaseFileBundle;
import com.dbn.connection.info.ConnectionInfo;
import com.dbn.connection.info.ui.ConnectionInfoDialog;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.connection.jdbc.ResourceStatus;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.connection.transaction.DatabaseTransactionManager;
import com.dbn.connection.transaction.TransactionAction;
import com.dbn.connection.transaction.ui.IdleConnectionDialog;
import com.dbn.connection.ui.ConnectionAuthenticationDialog;
import com.dbn.credentials.Secret;
import com.dbn.execution.ExecutionManager;
import com.dbn.execution.method.MethodExecutionManager;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettingsManager;
import com.dbn.vfs.DatabaseFileManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.NlsContexts.DialogTitle;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.dbn.common.component.Components.projectService;
import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.common.dispose.Failsafe.guarded;
import static com.dbn.common.util.Conditional.when;
import static com.dbn.common.util.Messages.options;
import static com.dbn.common.util.Messages.showErrorDialog;
import static com.dbn.common.util.Messages.showInfoDialog;
import static com.dbn.common.util.Messages.showWarningDialog;
import static com.dbn.connection.transaction.TransactionAction.actions;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.dbn.nls.NlsResources.txt;

@Slf4j
@Getter
@State(
    name = ConnectionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ConnectionManager extends ProjectComponentBase implements PersistentState, ProjectManagerListener {

    public static final String COMPONENT_NAME = "DBNavigator.Project.ConnectionManager";

    private final Timer idleConnectionCleaner;
    private final ConnectionBundle connectionBundle;
    private static ConnectionRef lastUsedConnection;

    public static ConnectionManager getInstance(@NotNull Project project) {
        return projectService(project, ConnectionManager.class);
    }

    private ConnectionManager(@NotNull Project project) {
        super(project, COMPONENT_NAME);
        connectionBundle = new ConnectionBundle(project);

        ProjectEvents.subscribe(project, this,
                ConnectionConfigListener.TOPIC,
                ConnectionConfigListener.whenChanged(id -> refreshObjects(id)));

        idleConnectionCleaner = new Timer("DBN - Idle Connection Cleaner");
        idleConnectionCleaner.schedule(new CloseIdleConnectionTask(), TimeUtil.Millis.ONE_MINUTE, TimeUtil.Millis.ONE_MINUTE);

        Disposer.register(this, connectionBundle);
    }

    @Override
    public void projectClosed() {
        ConnectionCache.releaseCache(getProject());
    }

    @Override
    public void disposeInner() {
        Disposer.dispose(idleConnectionCleaner);
        super.disposeInner();
    }

    @Nullable
    public static ConnectionHandler getLastUsedConnection() {
        return lastUsedConnection == null ? null : lastUsedConnection.get();
    }

    @Nullable
    public static ConnectionInfo getLastUsedConnectionInfo() {
        ConnectionHandler lastUsedConnection = getLastUsedConnection();
        return lastUsedConnection == null ? null : lastUsedConnection.getConnectionInfo();
    }

    static void setLastUsedConnection(@NotNull ConnectionHandler lastUsedConnection) {
        ConnectionManager.lastUsedConnection = lastUsedConnection.ref();
    }

    private void refreshObjects(ConnectionId connectionId) {
        ConnectionHandler connection = getConnection(connectionId);
        if (connection == null) return;

        Background.run(() -> {
            connection.resetCompatibilityMonitor();
            List<TransactionAction> actions = actions(TransactionAction.DISCONNECT);

            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
            List<DBNConnection> connections = connection.getConnections();
            for (DBNConnection conn : connections) {
                transactionManager.execute(connection, conn, actions, false, null);
            }
            connection.getObjectBundle().getObjectLists().refreshObjects();
        });
    }

    /*********************************************************
    *                        Custom                         *
    *********************************************************/
    public void testConnection(ConnectionHandler connection, SchemaId schemaId, SessionId sessionId, boolean showSuccessMessage, boolean showErrorMessage) {
        Project project = connection.getProject();
        Progress.prompt(project, connection, true,
                txt("prc.connection.title.TestingConnection"),
                txt("prc.connection.text.TestingConnection", connection.getQualifiedName()),
                progress -> {
                    ConnectionDatabaseSettings databaseSettings = connection.getSettings().getDatabaseSettings();
                    String connectionName = connection.getName();
                    try {
                        databaseSettings.validate();
                        connection.getConnection(sessionId, schemaId);
                        ConnectionHandlerStatusHolder connectionStatus = connection.getConnectionStatus();
                        connectionStatus.setValid(true);
                        connectionStatus.setConnected(true);
                        if (showSuccessMessage) {
                            showSuccessfulConnectionMessage(project, connectionName);
                        }
                    } catch (ProcessCanceledException e) {
                        conditionallyLog(e);
                    } catch (ConfigurationException e) {
                        conditionallyLog(e);
                        if (showErrorMessage) {
                            showInvalidConfigMessage(project, e);

                        }
                    } catch (Exception e) {
                        conditionallyLog(e);
                        if (showErrorMessage) {
                            showErrorConnectionMessage(project, connectionName, e);
                        }
                    }
                });

    }

    public void testConfigConnection(ConnectionSettings connectionSettings, boolean showMessageDialog) {
        Project project = connectionSettings.getProject();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        try {
            databaseSettings.validate();

            if (databaseSettings.isDatabaseInitialized()) {
                ensureAuthenticationProvided(databaseSettings, (authenticationInfo) ->
                        attemptConfigConnection(
                                connectionSettings,
                                authenticationInfo,
                                showMessageDialog));
            } else {
                promptDatabaseInitDialog(databaseSettings,
                        option -> when(option == 0, () ->
                                ensureAuthenticationProvided(databaseSettings,
                                        authInfo -> attemptConfigConnection(
                                                connectionSettings,
                                                authInfo,
                                                showMessageDialog))));
            }




        } catch (ConfigurationException e) {
            conditionallyLog(e);
            showInvalidConfigMessage(project, e);
        }
    }

    private void attemptConfigConnection(ConnectionSettings connectionSettings, AuthenticationInfo authentication, boolean showMessageDialog) {
        Project project = connectionSettings.getProject();
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        String connectionName = databaseSettings.getName();

        Progress.modal(project, null, true,
                txt("prc.connection.title.ConnectingToDatabase"),
                txt("prc.connection.text.AttemptingConnectionToDatabase", connectionName),
                progress -> {
            try {
                DBNConnection connection = ConnectionUtil.connect(connectionSettings, null, authentication, SessionId.TEST, false, null);
                Resources.close(connection);
                databaseSettings.setConnectivityStatus(ConnectivityStatus.VALID);
                if (showMessageDialog) {
                    showSuccessfulConnectionMessage(project, connectionName);
                }
            } catch (ProcessCanceledException e) {
                conditionallyLog(e);
                databaseSettings.setConnectivityStatus(ConnectivityStatus.UNKNOWN);
            } catch (Exception e) {
                conditionallyLog(e);
                databaseSettings.setConnectivityStatus(ConnectivityStatus.INVALID);
                if (showMessageDialog) {
                    showErrorConnectionMessage(project, connectionName, e);
                }
            }
        });
    }

    public void showConnectionInfo(ConnectionSettings connectionSettings, EnvironmentType environmentType) {
        ConnectionDatabaseSettings databaseSettings = connectionSettings.getDatabaseSettings();
        String connectionName = databaseSettings.getName();
        Project project = connectionSettings.getProject();

        try {
            databaseSettings.validate();
            ensureAuthenticationProvided(databaseSettings, (authenticationInfo) ->
                    Progress.modal(project, null, false,
                            txt("prc.connection.title.ConnectingToDatabase"),
                            txt("prc.connection.text.ConnectingToDatabase", connectionName),
                            progress -> {
                                try {
                                    DBNConnection connection = ConnectionUtil.connect(connectionSettings, null, authenticationInfo, SessionId.TEST, false, null);
                                    ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());
                                    Resources.close(connection);
                                    showConnectionInfoDialog(connectionInfo, connectionName, environmentType);
                                } catch (Exception e) {
                                    conditionallyLog(e);
                                    showErrorConnectionMessage(project, connectionName, e);
                                }
                            }));

        } catch (ConfigurationException e) {
            conditionallyLog(e);
            showInvalidConfigMessage(project, e);
        }
    }

    public void promptMissingConnection() {
        Project project = getProject();
        showInfoDialog(
                project, txt("msg.connection.title.NoConnectionsAvailable"), txt("msg.connection.info.NoConnectionsAvailable"),
                options(txt("app.connection.button.SetupConnection"), txt("msg.shared.button.Cancel")), 0,
                option -> when(option == 0, () -> {
                    ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
                    settingsManager.openProjectSettings(ConfigId.CONNECTIONS);
                }));
    }

    private void ensureAuthenticationProvided(
            @NotNull ConnectionDatabaseSettings databaseSettings,
            @NotNull Consumer<AuthenticationInfo> consumer) {

        AuthenticationInfo authenticationInfo = databaseSettings.getAuthenticationInfo().clone();
        if (!authenticationInfo.isProvided()) {
            promptAuthenticationDialog(null, authenticationInfo, consumer);
        } else {
            consumer.accept(authenticationInfo);
        }
    }

    void promptDatabaseInitDialog(ConnectionHandler connection, MessageCallback callback) {
        ConnectionDatabaseSettings databaseSettings = connection.getSettings().getDatabaseSettings();
        promptDatabaseInitDialog(databaseSettings, callback);
    }

    private void promptDatabaseInitDialog(ConnectionDatabaseSettings databaseSettings, MessageCallback callback) {
        DatabaseInfo databaseInfo = databaseSettings.getDatabaseInfo();
        if (databaseInfo.getUrlType() == DatabaseUrlType.FILE) {
            DatabaseFileBundle fileBundle = databaseInfo.getFileBundle();
            Project project = databaseSettings.getProject();
            if (fileBundle == null || fileBundle.isEmpty()) {
                showErrorDialog(project, txt("msg.connection.title.InvalidDatabaseConfiguration"), txt("msg.connection.error.DatabaseFileNotSpecified"));
            } else {
                String missingFiles = fileBundle
                        .getFiles()
                        .stream()
                        .filter(f -> f.isValid() && !f.isPresent())
                        .map(f -> f.getPath())
                        .collect(Collectors.joining("\n"));

                if (!Strings.isEmpty(missingFiles)) {
                    showWarningDialog(
                            project,
                            txt("msg.connection.title.DatabaseFileNotAvailable"),
                            txt("msg.connection.info.DatabaseFileNotAvailable", missingFiles),
                            options(txt("msg.shared.button.Create"), txt("msg.shared.button.Cancel")), 0,
                            callback);
                }
            }
        }
    }

     public void promptConnectDialog(ConnectionHandler connection, @Nullable @DialogTitle String title, MessageCallback callback) {
        String connectionName = connection.getName();
        showInfoDialog(
                connection.getProject(),
                title == null ? txt("msg.connection.title.NotConnectedToDatabase") : title,
                txt("msg.connection.info.NotConnectedToDatabase", connectionName),
                ConnectionAction.OPTIONS_CONNECT_CANCEL, 0,
                callback);
    }

    public void showErrorConnectionMessage(Project project, String connectionName, @Nullable Throwable e) {
        showErrorDialog(
                project,
                txt("msg.connection.title.ConnectionError"),
                e == null ?
                    txt("msg.connection.error.ConnectionErrorUnknown", connectionName) :
                    txt("msg.connection.error.ConnectionError", connectionName, e.getLocalizedMessage()));
    }

    void showSuccessfulConnectionMessage(Project project, String connectionName) {
        showInfoDialog(
                project,
                txt("msg.connection.title.ConnectionSuccessful"),
                txt("msg.connection.confirmation.ConnectionSuccessful", connectionName));
    }

    private void showInvalidConfigMessage(Project project, ConfigurationException e) {
        showErrorDialog(
                project,
                txt("msg.connection.title.InvalidConfiguration"),
                e.getLocalizedMessage());
    }

    public static void showConnectionInfoDialog(ConnectionHandler connection) {
        Dialogs.show(() -> new ConnectionInfoDialog(connection));
    }

    private static void showConnectionInfoDialog(ConnectionInfo connectionInfo, String connectionName, EnvironmentType environmentType) {
        Dialogs.show(() -> new ConnectionInfoDialog(null, connectionInfo, connectionName, environmentType));
    }

    void promptAuthenticationDialog(
            @Nullable ConnectionHandler connection,
            @NotNull AuthenticationInfo authenticationInfo,
            @NotNull Consumer<AuthenticationInfo> consumer) {

        Dialogs.show(
                () -> new ConnectionAuthenticationDialog(getProject(), connection, authenticationInfo),
                (dialog, exitCode) -> {
                    if (exitCode != DialogWrapper.OK_EXIT_CODE) return;

                    AuthenticationInfo newAuthenticationInfo = dialog.getAuthenticationInfo();
                    if (connection != null) {
                        AuthenticationInfo storedAuthenticationInfo = connection.getAuthenticationInfo();

                        if (dialog.isRememberCredentials()) {
                            // create snapshot of previous authentication secrets
                            Secret[] oldSecrets = storedAuthenticationInfo.getSecrets();

                            storedAuthenticationInfo.updateWith(newAuthenticationInfo);
                            storedAuthenticationInfo.updateSecrets(oldSecrets);
                        } else {
                            AuthenticationInfo temporaryAuthenticationInfo = newAuthenticationInfo.clone();
                            temporaryAuthenticationInfo.setTemporary(true);
                            connection.setTemporaryAuthenticationInfo(temporaryAuthenticationInfo);
                        }
                        connection.getInstructions().setAllowAutoConnect(true);
                    }
                    consumer.accept(newAuthenticationInfo);
                });
    }

    /*********************************************************
     *                     Miscellaneous                     *
     *********************************************************/
    @Nullable
    public ConnectionHandler getConnection(ConnectionId connectionId) {
        return getConnectionBundle().getConnection(connectionId);
     }

     public List<ConnectionHandler> getConnections(Predicate<ConnectionHandler> predicate) {
        return Lists.filtered(getConnections(), predicate);
     }

     public List<ConnectionHandler> getConnections() {
         return getConnectionBundle().getConnections();
     }

     public ConnectionHandler getActiveConnection(Project project) {
         ConnectionHandler connection = null;
         VirtualFile virtualFile = Editors.getSelectedFile(project);
         DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
         if (browserManager.getBrowserToolWindow().isActive() || virtualFile == null) {
             connection = browserManager.getActiveConnection();
         }

         if (connection == null && virtualFile != null) {
             FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
             connection = contextManager.getConnection(virtualFile);
         }

         return connection;
     }

    public boolean hasUncommittedChanges() {
        for (ConnectionHandler connection : getConnections()) {
            if (connection.hasUncommittedChanges()) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidConnectionId(ConnectionId connectionId) {
        return getConnection(connectionId) != null;
    }

    private class CloseIdleConnectionTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (isNotValid(ConnectionManager.this.getProject())) return;
                for (ConnectionHandler connection : getConnections()) {
                    resolveIdleStatus(connection);
                }
            } catch (Exception e){
                conditionallyLog(e);
                log.error("Failed to release idle connections", e);
            }
        }

        private void resolveIdleStatus(ConnectionHandler connection) {
            guarded(connection, c -> {
                if (isNotValid(c) || isNotValid(c.getProject())) return;

                List<TransactionAction> actions = actions(TransactionAction.DISCONNECT_IDLE);
                DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(getProject());
                List<DBNConnection> connections = c.getConnections(ConnectionType.MAIN, ConnectionType.SESSION);

                for (DBNConnection conn : connections) {
                    if (!conn.isIdle()) continue;
                    if (conn.is(ResourceStatus.RESOLVING_TRANSACTION)) continue;

                    int idleMinutes = conn.getIdleMinutes();
                    int idleMinutesToDisconnect = c.getSettings().getDetailSettings().getIdleMinutesToDisconnect();
                    if (idleMinutes > idleMinutesToDisconnect) {
                        if (conn.hasDataChanges()) {
                            conn.set(ResourceStatus.RESOLVING_TRANSACTION, true);
                            Dialogs.show(() -> new IdleConnectionDialog(c, conn));
                        } else {
                            transactionManager.execute(c, conn, actions, false, null);
                        }
                    }
                }
            });
        }
    }

    void disposeConnections(@NotNull List<ConnectionHandler> connections) {
        if (connections.isEmpty()) return;

        Dispatch.run(() -> {
            Project project = getProject();
            List<ConnectionId> connectionIds = ConnectionHandler.ids(connections);

            ExecutionManager executionManager = ExecutionManager.getInstance(project);
            executionManager.closeExecutionResults(connectionIds);

            DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(project);
            databaseFileManager.closeDatabaseFiles(connectionIds);

            MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
            methodExecutionManager.cleanupExecutionHistory(connectionIds);

            Background.run(() -> {
                for (ConnectionHandler connection : connections) {
                    connection.getConnectionPool().closeConnections();
                    Disposer.dispose(connection);
                }
            });
        });
    }

    /*********************************************************
     *                PersistentStateComponent               *
     *********************************************************/
    @Override
    @Nullable
    public Element getComponentState() {
        return null;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {}
}