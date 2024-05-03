package com.dbn.editor.data.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DataFindAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        datasetEditor.showSearchHeader();
/*
            FindModel findModel = findManager.getFindInFileModel();

            findManager.showFindDialog(findModel, new Runnable() {
                @Override
                public void run() {
                    datasetEditor.getEditorForm().showSearchPanel();
                }
            });
*/

    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        presentation.setText("Find Data...");
        presentation.setIcon(Icons.ACTION_FIND);

        if (datasetEditor == null) {
            presentation.setEnabled(false);
        } else {
            presentation.setEnabled(true);
            if (datasetEditor.isInserting() || datasetEditor.isLoading()) {
                presentation.setEnabled(false);
            }
        }

    }
}
