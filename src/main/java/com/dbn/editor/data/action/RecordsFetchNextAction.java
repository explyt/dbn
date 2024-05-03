package com.dbn.editor.data.action;

import com.dbn.common.dispose.Checks;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.editor.data.options.DataEditorSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RecordsFetchNextAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        DataEditorSettings settings = DataEditorSettings.getInstance(datasetEditor.getProject());
        datasetEditor.fetchNextRecords(settings.getGeneralSettings().getFetchBlockSize().value());
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        DataEditorSettings settings = DataEditorSettings.getInstance(project);
        presentation.setText("Fetch Next " + settings.getGeneralSettings().getFetchBlockSize().value() + " Records");
        presentation.setIcon(Icons.DATA_EDITOR_FETCH_NEXT_RECORDS);

        boolean enabled =
                Checks.isValid(datasetEditor) &&
                        datasetEditor.isLoaded() &&
                        datasetEditor.getConnection().isConnected() &&
                        !datasetEditor.isInserting() &&
                        !datasetEditor.isLoading() &&
                        !datasetEditor.isDirty() &&
                        !datasetEditor.getEditorTable().getModel().isResultSetExhausted();
        presentation.setEnabled(enabled);
    }
}