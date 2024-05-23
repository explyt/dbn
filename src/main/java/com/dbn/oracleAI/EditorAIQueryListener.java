package com.dbn.oracleAI;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class EditorAIQueryListener implements DocumentListener {
  private final Project project;
  private final DatabaseOracleAIManager manager;
  private final Set<Integer> detectedPatternHashes = new HashSet<>();

  public EditorAIQueryListener(Project project) {
    this.project = project;
    this.manager = project.getService(DatabaseOracleAIManager.class);
  }

  @Override
  public void documentChanged(@NotNull DocumentEvent event) {
    Document document = event.getDocument();
    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
    if (file != null && isEligibleForPatternDetection(file)) {
      String content = document.getText();
      String[] lines = content.split("\n");
      for (String line : lines) {
        if (line.startsWith("---") && line.endsWith(";")) {
          int hash = line.hashCode();
          if (!detectedPatternHashes.contains(hash)) {
            System.out.println("Detected unique pattern: " + line);
            detectedPatternHashes.add(hash);
            processQuery(line, document);
          }
        }
      }
    }
  }

  private void processQuery(String comment, Document document) {
    try {
      String prompt = comment.substring(3, comment.length() - 1);
      AIProfileItem currProfile = manager.getDefaultProfile();
      String answer = manager.queryOracleAI(prompt, ActionAIType.EXPLAINSQL, currProfile.getLabel(), currProfile.getModel().getApiName());
      appendLine(document, processText(answer), comment);
    } catch (QueryExecutionException | SQLException e) {
      Messages.showErrorDialog(project, e.getMessage());
    }
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

  private boolean isEligibleForPatternDetection(VirtualFile file) {
    return true;
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
