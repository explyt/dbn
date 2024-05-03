package com.dbn.editor.data.action;

import com.dbn.common.dispose.Checks;
import com.dbn.common.environment.EnvironmentManager;
import com.dbn.common.icon.Icons;
import com.dbn.editor.DBContentType;
import com.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecordInsertAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        datasetEditor.insertRecord();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Insert Record");
        presentation.setIcon(Icons.DATA_EDITOR_INSERT_RECORD);

        if (Checks.isValid(datasetEditor)) {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean isEnvironmentReadonlyData = environmentManager.isReadonly(datasetEditor.getDataset(), DBContentType.DATA);
            presentation.setVisible(!isEnvironmentReadonlyData && !datasetEditor.isReadonlyData());
            presentation.setEnabled(
                    datasetEditor.getConnection().isConnected() &&
                    !datasetEditor.isReadonly() &&
                    !datasetEditor.isInserting() &&
                    !datasetEditor.isLoading() &&
                    !datasetEditor.isDirty());

        } else {
            presentation.setEnabled(false);
        }
    }
}