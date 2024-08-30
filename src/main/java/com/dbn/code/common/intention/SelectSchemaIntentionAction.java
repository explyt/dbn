package com.dbn.code.common.intention;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Context;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.language.common.DBLanguagePsiFile;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.common.util.Editors.isMainEditor;
import static com.dbn.common.util.Files.isDbLanguagePsiFile;
import static com.dbn.connection.ConnectionHandler.isLiveConnection;

public class SelectSchemaIntentionAction extends GenericIntentionAction implements LowPriorityAction {
    @Override
    @NotNull
    public String getText() {
        return "Set current schema";
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.FILE_SCHEMA_MAPPING;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiElement psiElement) {
        if (isDatabaseAssistantPrompt(psiElement)) return false;

        PsiFile psiFile = psiElement.getContainingFile();
        if (!isDbLanguagePsiFile(psiFile)) return false;
        if (!isMainEditor(editor)) return false;

        VirtualFile file = psiFile.getVirtualFile();
        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        if (!contextManager.isSchemaSelectable(file)) return false;


        DBLanguagePsiFile dbFile = (DBLanguagePsiFile) psiFile;
        ConnectionHandler connection = dbFile.getConnection();
        return isLiveConnection(connection);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiElement psiElement) throws IncorrectOperationException {
        PsiFile psiFile = psiElement.getContainingFile();
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguageFile = (DBLanguagePsiFile) psiFile;
            FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
            DataContext dataContext = Context.getDataContext(editor);
            contextManager.promptSchemaSelector(dbLanguageFile.getVirtualFile(), dataContext, null);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return 3;
    }
}
