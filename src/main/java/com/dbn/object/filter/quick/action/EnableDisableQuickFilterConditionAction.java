package com.dbn.object.filter.quick.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.object.filter.quick.ui.ObjectQuickFilterConditionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class EnableDisableQuickFilterConditionAction extends BasicAction {
    private ObjectQuickFilterConditionForm conditionForm;

    public EnableDisableQuickFilterConditionAction(ObjectQuickFilterConditionForm conditionForm) {
        this.conditionForm = conditionForm;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setIcon(
                conditionForm.isActive() ?
                        Icons.COMMON_FILTER_ACTIVE :
                        Icons.COMMON_FILTER_INACTIVE);
        e.getPresentation().setText(
                conditionForm.isActive() ?
                        "Deactivate Condition" :
                        "Activate Condition");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        conditionForm.setActive(!conditionForm.isActive());
    }
}
