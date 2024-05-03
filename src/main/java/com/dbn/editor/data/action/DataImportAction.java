package com.dbn.editor.data.action;

import com.dbn.common.environment.EnvironmentManager;
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.dbn.editor.DBContentType;
import com.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class DataImportAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        Messages.showInfoDialog(project, "Not implemented", "Data import is not implemented yet.");
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Import Data");
        presentation.setIcon(Icons.DATA_IMPORT);

        if (isValid(datasetEditor)) {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean isEnvironmentReadonlyData = environmentManager.isReadonly(datasetEditor.getDataset(), DBContentType.DATA);
            presentation.setVisible(!isEnvironmentReadonlyData && !datasetEditor.isReadonlyData());
/*
            boolean enabled =
                    datasetEditor.getConnectionHandler().isConnected() &&
                    !datasetEditor.isReadonly() &&
                    !datasetEditor.isInserting();
*/
            boolean enabled = false;
            presentation.setEnabled(enabled);
        } else {
            presentation.setEnabled(false);
        }
    }
}