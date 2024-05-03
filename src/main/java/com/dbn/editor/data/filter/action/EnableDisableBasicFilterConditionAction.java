package com.dbn.editor.data.filter.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.filter.ui.DatasetBasicFilterConditionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class EnableDisableBasicFilterConditionAction extends BasicAction {
    private DatasetBasicFilterConditionForm conditionForm;

    public EnableDisableBasicFilterConditionAction(DatasetBasicFilterConditionForm conditionForm) {
        this.conditionForm = conditionForm;
    }

    @Override
    public void update(AnActionEvent e) {
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
