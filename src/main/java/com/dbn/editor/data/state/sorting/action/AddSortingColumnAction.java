package com.dbn.editor.data.state.sorting.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.state.sorting.ui.DatasetEditorSortingForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;


public class AddSortingColumnAction extends BasicAction {
    private final DatasetEditorSortingForm form;

    public AddSortingColumnAction(DatasetEditorSortingForm form) {
        super("Add Sorting Column ", null, Icons.ACTION_ADD);
        this.form = form;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        form.addSortingColumn(null);
    }
}
