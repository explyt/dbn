package com.dbn.diagnostics.action;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.action.AbstractConnectionAction;
import com.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiscellaneousConnectionAction extends AbstractConnectionAction {
    public MiscellaneousConnectionAction(@NotNull ConnectionHandler connection) {
        super(connection);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {

    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler connection) {
        presentation.setVisible(Diagnostics.isBulkActionsEnabled());
        presentation.setText("Dev Test");
    }
}
