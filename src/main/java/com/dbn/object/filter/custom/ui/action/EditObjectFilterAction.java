package com.dbn.object.filter.custom.ui.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.object.filter.custom.ObjectFilter;
import com.dbn.object.filter.custom.ui.ObjectFilterDetailsDialog;
import com.dbn.object.filter.custom.ui.ObjectFilterExpressionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class EditObjectFilterAction extends BasicAction {
    private final ObjectFilterExpressionForm form;

    public EditObjectFilterAction(ObjectFilterExpressionForm form) {
        this.form = form;
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setIcon(Icons.ACTION_EDIT);
        e.getPresentation().setText("Edit Filter");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ObjectFilter<?> filter = form.getFilter();
        form.getParentForm().showFilterDetailsDialog(filter, false, () -> form.setExpression(filter.getExpression()));
    }

}
