package com.dbn.editor.data.filter.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.data.filter.DatasetFilter;
import com.dbn.editor.data.filter.ui.DatasetFilterList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class CreateBasicFilterAction extends AbstractFilterListAction {

    public CreateBasicFilterAction(DatasetFilterList filterList) {
        super(filterList);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DatasetFilter filter = getFilterGroup().createBasicFilter(true);
        getFilterList().setSelectedValue(filter, true);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Basic Filter");
        presentation.setIcon(Icons.DATASET_FILTER_BASIC);
    }
}