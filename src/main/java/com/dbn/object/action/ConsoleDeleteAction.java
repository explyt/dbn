package com.dbn.object.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.connection.console.DatabaseConsoleManager;
import com.dbn.object.DBConsole;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConsoleDeleteAction extends ProjectAction {
    private final DBConsole console;

    ConsoleDeleteAction(DBConsole console) {
        this.console = console;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Delete Console");
        presentation.setIcon(Icons.ACTION_DELETE);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseConsoleManager consoleManager = DatabaseConsoleManager.getInstance(project);
        consoleManager.deleteConsole(console);
    }
}