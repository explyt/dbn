package com.dbn.data.record.navigation.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditorManager;
import com.dbn.editor.data.filter.DatasetFilterInput;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RecordEditorOpenAction extends ProjectAction {
    private final DatasetFilterInput filterInput;

    RecordEditorOpenAction(DatasetFilterInput filterInput) {
        this.filterInput = filterInput;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Editor");
        presentation.setIcon(Icons.CONNECTION_NEW);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(project);
        datasetEditorManager.openDataEditor(filterInput);
    }
}
