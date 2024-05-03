package com.dbn.editor.code.action;

import com.dbn.common.environment.EnvironmentManager;
import com.dbn.common.icon.Icons;
import com.dbn.editor.code.SourceCodeEditor;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class CompareWithOriginalAction extends AbstractCodeEditorDiffAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull SourceCodeEditor fileEditor, @NotNull DBSourceCodeVirtualFile sourceCodeFile) {
        CharSequence referenceText = sourceCodeFile.getOriginalContent();
        openDiffWindow(project, sourceCodeFile, referenceText.toString(), "Original version", "Local version");
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable SourceCodeEditor fileEditor, @Nullable DBSourceCodeVirtualFile sourceCodeFile) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Compare with Original");
        presentation.setIcon(Icons.CODE_EDITOR_DIFF);
        if (isValid(sourceCodeFile)) {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean readonly = environmentManager.isReadonly(sourceCodeFile);
            presentation.setVisible(!readonly);
            presentation.setEnabled(sourceCodeFile.isModified());
        } else {
            presentation.setEnabled(false);
        }
    }
}
