package com.dbn.object.filter.custom.ui.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.object.filter.custom.ui.ObjectFilterExpressionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ToggleObjectFilterStatusAction extends BasicAction {
    private ObjectFilterExpressionForm form;

    public ToggleObjectFilterStatusAction(ObjectFilterExpressionForm form) {
        this.form = form;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setIcon(
                form.isActive() ?
                        Icons.COMMON_FILTER_ACTIVE :
                        Icons.COMMON_FILTER_INACTIVE);
        e.getPresentation().setText(
                form.isActive() ?
                        "Deactivate Filter" :
                        "Activate Filter");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        form.setActive(!form.isActive());
    }
}
