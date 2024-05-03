package com.dbn.editor.code.action;

import com.dbn.common.dispose.Checks;
import com.dbn.common.icon.Icons;
import com.dbn.editor.DBContentType;
import com.dbn.editor.code.SourceCodeEditor;
import com.dbn.editor.code.SourceCodeManager;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.vfs.file.status.DBFileStatus.LOADING;
import static com.dbn.vfs.file.status.DBFileStatus.MODIFIED;

public class SourceCodeReloadAction extends AbstractCodeEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull SourceCodeEditor fileEditor, @NotNull DBSourceCodeVirtualFile sourceCodeFile) {
        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
        sourceCodeManager.loadSourceCode(sourceCodeFile, true);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable SourceCodeEditor fileEditor, @Nullable DBSourceCodeVirtualFile sourceCodeFile) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.CODE_EDITOR_RELOAD);

        if (Checks.isValid(sourceCodeFile)) {
            DBContentType contentType = sourceCodeFile.getContentType();
            String text =
                contentType == DBContentType.CODE_SPEC ? "Reload Spec" :
                contentType == DBContentType.CODE_BODY ? "Reload Body" : "Reload";

            presentation.setText(text);
            presentation.setEnabled(sourceCodeFile.isNot(LOADING) && sourceCodeFile.isNot(MODIFIED));
        } else {
            presentation.setEnabled(false);
        }
    }
}