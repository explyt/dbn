package com.dbn.object.filter.quick.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.object.filter.quick.ui.ObjectQuickFilterConditionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class DeleteQuickFilterConditionAction extends BasicAction {
    private final ObjectQuickFilterConditionForm conditionForm;

    public DeleteQuickFilterConditionAction(ObjectQuickFilterConditionForm conditionForm) {
        this.conditionForm = conditionForm;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Remove Condition");
        presentation.setIcon(Icons.ACTION_DELETE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        conditionForm.remove();
    }

}
