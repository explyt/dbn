package com.dbn.editor.data.action;

import com.dbn.common.dispose.Checks;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecordEditAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        datasetEditor.openRecordEditor();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Edit Record");
        presentation.setIcon(Icons.DATA_EDITOR_EDIT_RECORD);

        boolean enabled =
                Checks.isValid(datasetEditor) &&
                datasetEditor.getConnection().isConnected() &&
                datasetEditor.getEditorTable().getSelectedRow() != -1 &&
                !datasetEditor.isInserting() &&
                !datasetEditor.isLoading() &&
                !datasetEditor.isDirty();
        presentation.setEnabled(enabled);

    }
}