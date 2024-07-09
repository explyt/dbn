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

public class DatabaseConnectivityTestAction extends AbstractConnectionAction {

    DatabaseConnectivityTestAction(ConnectionHandler connection) {
        super(connection);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        connection.getInstructions().setAllowAutoConnect(true);
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);

        ConnectionAction.invoke(txt("app.connection.activity.TestingConnectivity"), true, connection,
                (action) -> connectionManager.testConnection(connection, null, SessionId.MAIN, true, true));
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler target) {
        presentation.setEnabled(target != null);
        presentation.setText(txt("app.connection.action.TestConnectivity"));
        presentation.setDescription(target == null ? null : txt("app.connection.action.TestConnectivityTo", target.getName()));
    }
}
