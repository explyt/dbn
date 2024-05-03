package com.dbn.connection.transaction.action;

import com.dbn.connection.action.AbstractConnectionAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PendingTransactionsOpenAction extends AbstractConnectionAction {

    public PendingTransactionsOpenAction(ConnectionHandler connection) {
        super(connection);

    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
        transactionManager.showPendingTransactionsDialog(connection, null);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionHandler connection) {
        boolean enabled = connection != null && connection.hasUncommittedChanges();

        presentation.setEnabled(enabled);
        presentation.setText("Show Uncommitted Changes");
    }
}
