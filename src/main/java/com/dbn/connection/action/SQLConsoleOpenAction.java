package com.dbn.connection.action;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Editors;
import com.dbn.connection.ConnectionHandler;
import com.dbn.object.DBConsole;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLConsoleOpenAction extends AbstractConnectionAction {
    SQLConsoleOpenAction(ConnectionHandler connection) {
        super(connection);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler target) {
        presentation.setText("Open SQL Console");
        presentation.setIcon(Icons.SQL_CONSOLE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        DBConsole defaultConsole = connection.getConsoleBundle().getDefaultConsole();
        Editors.openFileEditor(project, defaultConsole.getVirtualFile(), true);
    }
}
