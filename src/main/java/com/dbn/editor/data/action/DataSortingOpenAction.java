package com.dbn.editor.data.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.editor.data.state.DatasetEditorStateManager;
import com.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class DataSortingOpenAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        DBDataset dataset = datasetEditor.getDataset();
        DatasetEditorStateManager stateManager = DatasetEditorStateManager.getInstance(datasetEditor.getProject());
        stateManager.openSortingDialog(datasetEditor);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Data Sorting...");
        presentation.setIcon(Icons.DATA_SORTING);

        boolean enabled =
                isValid(datasetEditor) &&
                !datasetEditor.isInserting() &&
                !datasetEditor.isLoading();
        presentation.setEnabled(enabled);
    }
}
