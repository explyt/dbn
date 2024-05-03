package com.dbn.editor.data.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.editor.data.state.DatasetEditorStateManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class ColumnSetupOpenAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        DatasetEditorStateManager stateManager = DatasetEditorStateManager.getInstance(datasetEditor.getProject());
        stateManager.openColumnSetupDialog(datasetEditor);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Column Setup...");
        presentation.setIcon(Icons.DATA_COLUMNS);

        boolean enabled =
                isValid(datasetEditor) &&
                !datasetEditor.isInserting() &&
                !datasetEditor.isLoading();
        presentation.setEnabled(enabled);
    }
}
