package com.dbn.oracleAI;


import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.database.DatabaseFeature;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * This action runs when we select a text in the console and hit right click and chose "Show Sql".
 * It displays the sql result right under the selected text.
 */
public class ShowSqlAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    // TODO cleanup code duplication (see ShowSqlExplanationAction / )
    Project project = e.getProject();
    if (project == null) return;

    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor == null) return;

    SelectionModel selectionModel = editor.getSelectionModel();
    String selectedText = selectionModel.getSelectedText();
    if (selectedText == null || selectedText.isEmpty()) return;

    VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (file == null) return;

    FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
    ConnectionHandler connection = contextManager.getConnection(file);
    if (connection == null) return;
    if (!DatabaseFeature.AI_ASSISTANT.isSupported(connection)) return;


    new ShowSqlOnEditor(project).processQuery(connection.getConnectionId(), selectedText, editor.getDocument(), false);
  }

}
