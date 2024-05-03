package com.dbn.editor.data.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.editor.data.DatasetLoadInstructions;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;
import static com.dbn.editor.data.DatasetLoadInstruction.*;

public class DataReloadAction extends AbstractDataEditorAction {

    private static final DatasetLoadInstructions LOAD_INSTRUCTIONS = new DatasetLoadInstructions(USE_CURRENT_FILTER, PRESERVE_CHANGES, DELIBERATE_ACTION);

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        datasetEditor.loadData(LOAD_INSTRUCTIONS);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Reload");
        presentation.setIcon(Icons.DATA_EDITOR_RELOAD_DATA);

        boolean enabled =
                isValid(datasetEditor) &&
                datasetEditor.isLoaded() &&
                !datasetEditor.isInserting() &&
                !datasetEditor.isLoading();
        presentation.setEnabled(enabled);

    }
}