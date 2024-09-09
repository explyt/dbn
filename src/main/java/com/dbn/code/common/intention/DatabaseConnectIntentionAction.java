package com.dbn.code.common.intention;

import com.dbn.common.icon.Icons;
import com.dbn.connection.*;
import com.dbn.connection.session.DatabaseSession;
import com.dbn.language.common.DBLanguagePsiFile;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.connection.ConnectionHandler.isLiveConnection;

public class DatabaseConnectIntentionAction extends GenericIntentionAction implements LowPriorityAction{
    @Override
    @NotNull
    public String getText() {
        return "Connect to database";
    }


    @Override
    public Icon getIcon(int flags) {
        return Icons.CONNECTION_CONNECTED;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (isDatabaseAssistantPrompt(editor, psiElement)) return false;

        PsiFile psiFile = psiElement.getContainingFile();
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            ConnectionHandler connection = dbLanguagePsiFile.getConnection();
            if (isLiveConnection(connection) && !connection.canConnect() && !connection.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiElement psiElement) throws IncorrectOperationException {
        PsiFile psiFile = psiElement.getContainingFile();
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            ConnectionHandler connection = dbLanguagePsiFile.getConnection();
            if (isLiveConnection(connection)) {
                connection.getInstructions().setAllowAutoConnect(true);

                DatabaseSession databaseSession = dbLanguagePsiFile.getSession();
                SessionId sessionId = databaseSession == null ? SessionId.MAIN : databaseSession.getId();
                SchemaId schemaId = dbLanguagePsiFile.getSchemaId();

                ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                ConnectionAction.invoke(null, true, connection,
                        (action) -> connectionManager.testConnection(connection, schemaId, sessionId, false, true));
            }
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return super.getGroupPriority();
    }
}
