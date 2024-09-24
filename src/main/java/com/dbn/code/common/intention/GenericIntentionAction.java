package com.dbn.code.common.intention;

import com.dbn.connection.ConnectionHandler;
import com.dbn.language.common.DBLanguagePsiFile;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.assistant.editor.AssistantEditorActionUtil.isAssistantSupported;
import static com.dbn.assistant.editor.AssistantEditorActionUtil.resolvePromptText;

public abstract class GenericIntentionAction extends PsiElementIntentionAction implements IntentionAction, PriorityAction, Iconable, DumbAware, Comparable {

    @Override
    @NotNull
    public String getFamilyName() {
        return getText();
    }

    @Nullable
    protected ConnectionHandler getConnection(PsiFile psiFile) {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            return dbLanguagePsiFile.getConnection();
        }
        return null;
    }

    protected Integer getGroupPriority() {
        return 0;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (o instanceof GenericIntentionAction) {
            GenericIntentionAction a = (GenericIntentionAction) o;
            int groupLevel = getPriority().compareTo(a.getPriority());

            return groupLevel == 0 ? getGroupPriority().compareTo(a.getGroupPriority()) : groupLevel;
        }
        return 0;
    }

    /**
     * Verifies if the element where intention has been invoked is a database assistant comment
     * To be used to expose the DatabaseAssistant intention actions, but also hide all other intentions
     *
     * @param editor the editor from the intention context
     * @param element the element from the intention context
     * @return true if the element is an AI-Assistant comment (starting with three dashes), false otherwise
     */
    protected boolean isDatabaseAssistantPrompt(Editor editor, PsiElement element) {
        return isAssistantSupported(editor) && resolvePromptText(editor, element) != null;
    }
}
