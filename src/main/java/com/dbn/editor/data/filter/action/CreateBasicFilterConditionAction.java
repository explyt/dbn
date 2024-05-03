package com.dbn.editor.data.filter.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.filter.ui.DatasetBasicFilterForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;


public class CreateBasicFilterConditionAction extends BasicAction {
    private final DatasetBasicFilterForm filterForm;

    public CreateBasicFilterConditionAction(DatasetBasicFilterForm filterForm) {
        super("Add condition", null, Icons.ACTION_ADD);
        this.filterForm = filterForm;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        filterForm.addConditionPanel(null);
    }
}
