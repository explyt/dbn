package com.dbn.editor.data.action;

import com.dbn.common.dispose.Checks;
import com.dbn.common.environment.EnvironmentManager;
import com.dbn.common.icon.Icons;
import com.dbn.editor.DBContentType;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecordDuplicateAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        datasetEditor.duplicateRecord();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Duplicate Record");
        presentation.setIcon(Icons.DATA_EDITOR_DUPLICATE_RECORD);

        if (Checks.isValid(datasetEditor) && datasetEditor.getConnection().isConnected()) {
            presentation.setEnabled(true);
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean isEnvironmentReadonlyData = environmentManager.isReadonly(datasetEditor.getDataset(), DBContentType.DATA);

            presentation.setVisible(!isEnvironmentReadonlyData && !datasetEditor.isReadonlyData());
            if (datasetEditor.isInserting() || datasetEditor.isLoading() || datasetEditor.isDirty() || datasetEditor.isReadonly()) {
                presentation.setEnabled(false);
            } else {
                DatasetEditorTable editorTable = datasetEditor.getEditorTable();
                int[] selectedRows = editorTable.getSelectedRows();
                presentation.setEnabled(selectedRows != null && selectedRows.length == 1 && selectedRows[0] < editorTable.getModel().getRowCount());
            }
        } else {
            presentation.setEnabled(false);
        }
    }
}