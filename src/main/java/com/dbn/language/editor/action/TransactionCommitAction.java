package com.dbn.language.editor.action;

import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TransactionCommitAction extends TransactionEditorAction {

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        super.update(e, project);

        Presentation presentation = e.getPresentation();
        presentation.setText("Commit");
        presentation.setDescription("Commit changes");
        presentation.setIcon(Icons.CONNECTION_COMMIT);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ConnectionHandler connection = getConnection(e);
        if (connection == null) return;

        DBNConnection conn = getTargetConnection(e);
        if (conn == null) return;

        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
        transactionManager.commit(connection, conn, true, false, null);
    }
}