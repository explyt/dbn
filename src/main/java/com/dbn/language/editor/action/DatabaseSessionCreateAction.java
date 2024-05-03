package com.dbn.language.editor.action;

import com.dbn.common.action.Lookups;
import com.dbn.common.action.ProjectAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.connection.session.DatabaseSessionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DatabaseSessionCreateAction extends ProjectAction {
    private final ConnectionRef connection;

    DatabaseSessionCreateAction(ConnectionHandler connection) {
        this.connection = connection.ref();
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookups.getEditor(e);
        if (editor == null) return;

        DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
        ConnectionHandler connection = this.connection.ensure();
        sessionManager.showCreateSessionDialog(
                connection,
                (session) -> {
                    if (session != null) {
                        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
                        contextManager.setDatabaseSession(editor, session);
                    }
                });
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setText("New Session...");
    }
}
