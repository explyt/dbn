package com.dbn.editor.data.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.editor.data.filter.DatasetFilter;
import com.dbn.editor.data.filter.DatasetFilterManager;
import com.dbn.editor.data.filter.DatasetFilterType;
import com.dbn.editor.data.options.DataEditorSettings;
import com.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class DatasetFilterCreateEditAction extends AbstractDataEditorAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        DBDataset dataset = datasetEditor.getDataset();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter == null || activeFilter.getFilterType() == DatasetFilterType.NONE) {
            DataEditorSettings settings = DataEditorSettings.getInstance(dataset.getProject());
            DatasetFilterType filterType = settings.getFilterSettings().getDefaultFilterType();
            if (filterType == null || filterType == DatasetFilterType.NONE) {
                filterType = DatasetFilterType.BASIC;
            }


            filterManager.openFiltersDialog(dataset, false, true, filterType, null);
        }
        else {
            filterManager.openFiltersDialog(dataset, false, false,DatasetFilterType.NONE, null);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        if (isValid(datasetEditor) && datasetEditor.getConnection().isConnected()) {
            DBDataset dataset = datasetEditor.getDataset();
            boolean enabled = !datasetEditor.isInserting() && !datasetEditor.isLoading();

            presentation.setEnabled(enabled);

            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
            DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
            if (activeFilter == null || activeFilter.getFilterType() == DatasetFilterType.NONE) {
                presentation.setText("Create Filter");
                presentation.setIcon(Icons.DATASET_FILTER_NEW);
            } else {
                presentation.setText("Edit Filter");
                presentation.setIcon(Icons.DATASET_FILTER_EDIT);
            }
        } else {
            presentation.setEnabled(false);
        }
    }
}
