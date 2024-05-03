package com.dbn.editor.code.action;

import com.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

abstract class AbstractCodeEditorDiffAction extends AbstractCodeEditorAction {
    public AbstractCodeEditorDiffAction() {
    }

    void openDiffWindow(
            @NotNull Project project,
            @NotNull DBSourceCodeVirtualFile sourceCodeFile,
            String referenceText,
            String referenceTitle,
            String windowTitle) {
        SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
        diffManager.openDiffWindow(sourceCodeFile, referenceText, referenceTitle, windowTitle);
    }
}

