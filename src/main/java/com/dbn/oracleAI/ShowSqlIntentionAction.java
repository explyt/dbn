package com.dbn.oracleAI;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class ShowSqlIntentionAction extends PsiElementBaseIntentionAction {

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
    String text = element.getText();
    return text.startsWith("---") && text.endsWith(";\n");
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
    String line = element.getText();
    new ShowSqlOnEditor(project).processQuery(line, editor.getDocument(), true);
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getText();
  }

  public static void register() {
    IntentionManagerImpl.getInstance().addAction(new ShowSqlIntentionAction());
  }

  @Override
  @NotNull
  public String getText() {
    return "Show SQL Query";
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

}
