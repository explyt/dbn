package com.dbn.editor.data.filter.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.data.filter.DatasetFilter;
import com.dbn.editor.data.filter.ui.DatasetFilterList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeleteFilterAction extends AbstractFilterListAction {

    public DeleteFilterAction(DatasetFilterList filterList) {
        super(filterList);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<DatasetFilter> selectedFilters = getFilterList().getSelectedValuesList();
        for (DatasetFilter filter : selectedFilters) {
            getFilterGroup().deleteFilter(filter);
            if (getFilterList().getModel().getSize() > 0) {
                getFilterList().setSelectedIndex(0);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Delete Filter");
        presentation.setIcon(Icons.ACTION_REMOVE);
    }
}
