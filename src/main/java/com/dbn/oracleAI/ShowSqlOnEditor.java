package com.dbn.oracleAI;

import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;

/**
 * This is the class that handles processing the prompt from the console and displaying the results to it.
 */
public class ShowSqlOnEditor {
  private final Project project;
  private final DatabaseOracleAIManager manager;

  public ShowSqlOnEditor(Project project) {
    this.project = project;
    this.manager = project.getService(DatabaseOracleAIManager.class);
  }

  public void processQuery(String comment, Document document) {
    String prompt = comment.substring(3, comment.length() - 1);
    AIProfileItem currProfile = manager.getDefaultProfile();
    manager.queryOracleAI(prompt, ActionAIType.EXPLAINSQL, currProfile.getLabel(), currProfile.getModel().getApiName())
        .thenAccept(answer -> appendLine(document, processText(answer), comment))
        .exceptionally((e) -> {
          com.dbn.common.util.Messages.showErrorDialog(project, e.getMessage());
          return null;
        });
  }


  private void appendLine(Document document, String lineToAppend, String afterComment) {
    ApplicationManager.getApplication().invokeLater(() -> {
      WriteCommandAction.runWriteCommandAction(project, () -> {
        String content = document.getText();
        int pos = content.indexOf(afterComment);
        if (pos >= 0) {
          pos = content.indexOf("\n", pos);
          if (pos >= 0) {
            document.insertString(pos + 1, lineToAppend + "\n");
          } else {
            document.insertString(content.length(), "\n" + lineToAppend + "\n");
          }
        } else {
          document.insertString(document.getTextLength(), "\n" + lineToAppend);
        }
      });
    });
  }

  public static String processText(String input) {
    StringBuilder result = new StringBuilder();
    boolean inCodeBlock = false;
    boolean inSqlQuery = false;
    String[] lines = input.split("\n");
    result.append("\n");

    for (String line : lines) {
      if (line.trim().startsWith("```")) {
        inCodeBlock = !inCodeBlock;
        result.append("\n");
        continue;
      }

      if (inCodeBlock) {
        result.append(line).append("\n");
      } else {
        if (inSqlQuery || line.trim().toUpperCase().contains("SELECT")) {
          inSqlQuery = true;
          result.append(line.trim()).append("\n");

          if (line.trim().endsWith(";")) {
            inSqlQuery = false;
          }
        } else {
          if (inSqlQuery) {
            inSqlQuery = false;
            result.append("-- ").append(line.trim()).append("\n");
          } else {
            result.append("-- ").append(line).append("\n");
          }
        }
      }
    }
    if (inSqlQuery) {
      result.append("-- Incomplete SQL query above\n");
    }

    return result.toString();
  }
}
