package com.dbn.code.common.intention;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Context;
import com.dbn.common.util.Editors;
import com.dbn.execution.statement.StatementExecutionManager;
import com.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dbn.language.common.psi.ExecutablePsiElement;
import com.dbn.language.common.psi.PsiUtil;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.common.util.Editors.isMainEditor;
import static com.dbn.common.util.Files.isDbLanguageFile;
import static com.dbn.connection.mapping.FileConnectionContextManager.hasConnectivityContext;
import static com.dbn.debugger.DatabaseDebuggerManager.isDebugConsole;

public class ExecuteStatementIntentionAction extends GenericIntentionAction implements HighPriorityAction {
    @Override
    @NotNull
    public String getText() {
        return "Execute statement";
    }


    @Override
    public Icon getIcon(int flags) {
        return Icons.STMT_EXECUTION_RUN;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiElement psiElement) {
        if (isDatabaseAssistantPrompt(psiElement)) return false;

        PsiFile psiFile = psiElement.getContainingFile();
        if (isNotValid(psiFile)) return false;

        VirtualFile file = psiFile.getVirtualFile();
        if (isNotValid(file)) return false;
        if (isDebugConsole(file)) return false;
        if (!isDbLanguageFile(file)) return false;
        if (!hasConnectivityContext(file)) return false;
        if (!isMainEditor(editor)) return false;

        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
        if (isNotValid(executable)) return false;

        FileEditor fileEditor = Editors.getFileEditor(editor);
        if (isNotValid(fileEditor)) return false;

        return true;
/*
            StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
            StatementExecutionProcessor executionProcessor = executionManager.getExecutionProcessor(fileEditor, executable, true);
            if (executionProcessor != null) {
                StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
                return executionResult != null;
            }
*/
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiElement psiElement) throws IncorrectOperationException {
        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(editor, true);
        if (isNotValid(executable)) return;

        FileEditor fileEditor = Editors.getFileEditor(editor);
        if (isNotValid(fileEditor)) return;

        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        StatementExecutionProcessor executionProcessor = executionManager.getExecutionProcessor(fileEditor, executable, true);
        if (isNotValid(executionProcessor)) return;

        DataContext dataContext = Context.getDataContext(editor);
        executionManager.executeStatement(executionProcessor, dataContext);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    protected Integer getGroupPriority() {
        return 0;
    }
}
