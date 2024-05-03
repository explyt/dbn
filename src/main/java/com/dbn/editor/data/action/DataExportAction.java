package com.dbn.editor.data.action;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Dialogs;
import com.dbn.data.export.ui.ExportDataDialog;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataExportAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        DBDataset dataset = datasetEditor.getDataset();
        Dialogs.show(() -> new ExportDataDialog(datasetEditor.getEditorTable(), dataset));
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Export Data");
        presentation.setIcon(Icons.DATA_EXPORT);

        boolean enabled =
                datasetEditor != null &&
                !datasetEditor.isInserting();
        presentation.setEnabled(enabled);

    }
}
