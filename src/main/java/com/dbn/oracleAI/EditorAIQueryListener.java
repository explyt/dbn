//package com.dbn.oracleAI;
//
//import com.intellij.openapi.editor.Document;
//import com.intellij.openapi.editor.event.DocumentEvent;
//import com.intellij.openapi.editor.event.DocumentListener;
//import com.intellij.openapi.fileEditor.FileDocumentManager;
//import com.intellij.openapi.project.Project;
//import com.intellij.openapi.vfs.VirtualFile;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.HashSet;
//import java.util.Set;
//
///**
// * This listener listens for a certain pattern (--- ...... ;).
// * It displays the sql result right under the detected pattern position.
// */
//public class EditorAIQueryListener implements DocumentListener {
//  private final Project project;
//  private final Set<Integer> detectedPatternHashes = new HashSet<>();
//
//  public EditorAIQueryListener(Project project) {
//    this.project = project;
//  }
//
//  @Override
//  public void documentChanged(@NotNull DocumentEvent event) {
//    Document document = event.getDocument();
//    VirtualFile file = FileDocumentManager.getInstance().getFile(document);
//    if (file != null) {
//      String content = document.getText();
//      String[] lines = content.split("\n");
//      for (String line : lines) {
//        if (line.startsWith("---") && line.endsWith(";")) {
//          int hash = line.hashCode();
//          if (!detectedPatternHashes.contains(hash)) {
//            detectedPatternHashes.add(hash);
//            new ShowSqlOnEditor(project).processQuery(line, document, true);
//          }
//        }
//      }
//    }
//  }
//
//
//}
