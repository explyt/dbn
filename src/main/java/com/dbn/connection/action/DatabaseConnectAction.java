package com.dbn.connection.action;

import com.dbn.connection.ConnectionAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionManager;
import com.dbn.connection.SessionId;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseConnectAction extends AbstractConnectionAction {
    DatabaseConnectAction(ConnectionHandler connection) {
        super(connection);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        connection.getInstructions().setAllowAutoConnect(true);
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);

        ConnectionAction.invoke(null, true, connection,
                (action) -> connectionManager.testConnection(connection, null, SessionId.MAIN, false, true));
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler target) {
        boolean enabled = target != null && !target.getConnectionStatus().isConnected();

        presentation.setText(txt("app.connection.action.Connect"));
        presentation.setDescription(target == null ? null : txt("app.connection.action.ConnectTo", target.getName()));
        presentation.setEnabled(enabled);
    }

}
