package com.dbn.editor.data.state.sorting.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.state.sorting.ui.DatasetSortingColumnForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class DeleteSortingCriteriaAction extends BasicAction {
    private final DatasetSortingColumnForm form;

    public DeleteSortingCriteriaAction(DatasetSortingColumnForm form) {
        this.form = form;
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(Icons.ACTION_CLOSE);
        e.getPresentation().setText("Remove Sorting Criteria");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        form.remove();
    }

}
