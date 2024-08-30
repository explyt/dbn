package com.dbn.code.common.intention;

import com.dbn.common.icon.Icons;
import com.dbn.debugger.DatabaseDebuggerManager;
import com.dbn.object.DBMethod;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DebugMethodIntentionAction extends AbstractMethodExecutionIntentionAction {

    @Override
    protected String getActionName() {
        return "Debug method";
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.METHOD_EXECUTION_DEBUG;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiElement psiElement) {
        if (isDatabaseAssistantPrompt(psiElement)) return false;

        PsiFile psiFile = psiElement.getContainingFile();
        if (psiFile != null) {
            DBMethod method = resolveMethod(editor, psiFile);
            return method != null;
        }
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiElement psiElement) throws IncorrectOperationException {
        PsiFile psiFile = psiElement.getContainingFile();
        DBMethod method = resolveMethod(editor, psiFile);
        if (method != null) {
            DatabaseDebuggerManager executionManager = DatabaseDebuggerManager.getInstance(project);
            executionManager.startMethodDebugger(method);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return 1;
    }
}
