package com.dbn.object.filter.custom.ui.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.object.filter.custom.ui.ObjectFilterExpressionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class DeleteObjectFilterAction extends BasicAction {
    private final ObjectFilterExpressionForm form;

    public DeleteObjectFilterAction(ObjectFilterExpressionForm conditionForm) {
        this.form = conditionForm;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Remove Filter");
        presentation.setIcon(Icons.ACTION_DELETE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        form.remove();
    }

}
