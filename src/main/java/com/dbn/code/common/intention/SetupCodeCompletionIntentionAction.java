package com.dbn.code.common.intention;

import com.dbn.language.common.DBLanguagePsiFile;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettingsManager;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SetupCodeCompletionIntentionAction extends GenericIntentionAction implements LowPriorityAction {
    @Override
    @NotNull
    public String getText() {
        return "Setup code completion";
    }

    @Override
    public Icon getIcon(int flags) {
        return null;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiElement psiElement) {
        if (isDatabaseAssistantPrompt(psiElement)) return false;

        PsiFile psiFile = psiElement.getContainingFile();
        return psiFile instanceof DBLanguagePsiFile && psiFile.getVirtualFile().getParent() != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        settingsManager.openProjectSettings(ConfigId.CODE_COMPLETION);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}