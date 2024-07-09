package com.dbn.connection.action;

import com.dbn.common.thread.Progress;
import com.dbn.connection.ConnectionAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseInformationOpenAction extends AbstractConnectionAction {

    DatabaseInformationOpenAction(ConnectionHandler connection) {
        super(connection);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler target) {
        presentation.setText(txt("app.connection.action.ConnectionInfo"));
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        ConnectionAction.invoke(txt("app.connection.activity.ShowingDatabaseInformation"), true, connection,
                action -> Progress.prompt(project, connection, false,
                        txt("prc.workspace.title.LoadingDatabaseInformation"),
                        txt("prc.workspace.message.LoadingDatabaseInformation", connection.getName()),
                        progress -> ConnectionManager.showConnectionInfoDialog(connection)));
    }
}
