package com.dbn.editor.data.action;

import com.dbn.common.action.BasicAction;
import com.dbn.editor.data.DatasetEditorManager;
import com.dbn.editor.data.filter.DatasetFilter;
import com.dbn.editor.data.filter.DatasetFilterManager;
import com.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DatasetFilterSelectAction extends BasicAction {
    private DBDataset dataset;
    private DatasetFilter filter;

    protected DatasetFilterSelectAction(DBDataset dataset, DatasetFilter filter) {
        this.dataset = dataset;
        this.filter = filter;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = dataset.getProject();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter != filter) {
            filterManager.setActiveFilter(dataset, filter);
            DatasetEditorManager.getInstance(project).reloadEditorData(dataset);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(filter.getIcon());
        presentation.setText(filter.getName(), false);
        //presentation.setEnabled(dataset.getCache().isConnected());
        //e.getPresentation().setText(filter.getName());
    }
}
