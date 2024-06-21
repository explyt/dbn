package com.dbn.oracleAI;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ShowSqlExplanationAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    assert project != null;

    Editor editor = e.getData(CommonDataKeys.EDITOR);
    assert editor != null;

    SelectionModel selectionModel = editor.getSelectionModel();
    String selectedText = selectionModel.getSelectedText();
    if (selectedText == null || selectedText.isEmpty()) {
      return;
    }

    new ShowSqlOnEditor(project).processQuery(selectedText, editor.getDocument(), true);
  }
}
