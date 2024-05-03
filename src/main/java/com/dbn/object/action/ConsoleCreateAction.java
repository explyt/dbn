package com.dbn.object.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.connection.console.DatabaseConsoleManager;
import com.dbn.vfs.DBConsoleType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConsoleCreateAction extends ProjectAction {
    private final DBConsoleType consoleType;
    private final ConnectionRef connection;

    ConsoleCreateAction(ConnectionHandler connection, DBConsoleType consoleType) {
        this.connection = connection.ref();
        this.consoleType = consoleType;

    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseConsoleManager consoleManager = DatabaseConsoleManager.getInstance(project);
        consoleManager.showCreateConsoleDialog(connection.get(), consoleType);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setText("New " + consoleType.getName() + "...");
    }
}