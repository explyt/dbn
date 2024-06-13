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

  public void processQuery(String comment, Document document, boolean withExplanation) {
    String prompt = comment.substring(3, comment.length() - 1);
    AIProfileItem currProfile = manager.getDefaultProfile();
    manager.queryOracleAI(prompt, withExplanation ? ActionAIType.EXPLAINSQL : ActionAIType.SHOWSQL, currProfile.getLabel(), currProfile.getModel().getApiName())
        .thenAccept(answer -> appendLine(document, processText(answer, withExplanation), comment))
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

  public static String processText(String input, boolean withExplanation) {
    StringBuilder result = new StringBuilder();
    boolean inCodeBlock = false;
    String[] lines = input.split("\n");
    result.append("\n");

    if (!withExplanation) {
      for (String line : lines) {
        result.append(line).append("\n");
      }
    } else {
      for (String line : lines) {
        if (line.trim().startsWith("```")) {
          inCodeBlock = !inCodeBlock;
          result.append("\n");
          continue;
        }

        if (inCodeBlock) {
          result.append(line).append("\n");
        } else {
          result.append("-- ").append(line).append("\n");
        }
      }
    }

    return result.toString();
  }
}
