package com.dbn.editor.data.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.editor.data.filter.DatasetFilterManager;
import com.dbn.editor.data.filter.DatasetFilterType;
import com.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class DatasetFilterOpenAction extends BasicAction {
    private DatasetEditor datasetEditor;
    DatasetFilterOpenAction(DatasetEditor datasetEditor) {
        super("Manage Filters...", null, Icons.ACTION_EDIT);
        this.datasetEditor = datasetEditor;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (datasetEditor != null) {
            DBDataset dataset = datasetEditor.getDataset();
            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
            filterManager.openFiltersDialog(dataset, false, false, DatasetFilterType.NONE, null);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        boolean enabled = datasetEditor != null && !datasetEditor.isInserting();
        e.getPresentation().setEnabled(enabled);

    }
}
