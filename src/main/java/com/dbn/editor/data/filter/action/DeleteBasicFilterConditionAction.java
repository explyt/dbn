package com.dbn.editor.data.filter.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.filter.ui.DatasetBasicFilterConditionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class DeleteBasicFilterConditionAction extends BasicAction {
    private DatasetBasicFilterConditionForm conditionForm;

    public DeleteBasicFilterConditionAction(DatasetBasicFilterConditionForm conditionForm) {
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
