package com.dbn.editor.data.state.sorting.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.data.sorting.SortDirection;
import com.dbn.data.sorting.SortingInstruction;
import com.dbn.editor.data.state.sorting.ui.DatasetSortingColumnForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ChangeSortingDirectionAction extends BasicAction {
    private final DatasetSortingColumnForm form;

    public ChangeSortingDirectionAction(DatasetSortingColumnForm form) {
        this.form = form;
    }

    @Override
    public void update(AnActionEvent e) {
        SortingInstruction sortingInstruction = form.getSortingInstruction();
        SortDirection direction = sortingInstruction.getDirection();
        Icon icon =
            direction == SortDirection.ASCENDING ? Icons.DATA_SORTING_ASC :
            direction == SortDirection.DESCENDING ? Icons.DATA_SORTING_DESC : null;
        e.getPresentation().setIcon(icon);
        e.getPresentation().setText("Change Sorting Direction");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        form.getSortingInstruction().switchDirection();
    }

}
