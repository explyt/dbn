package com.dbn.execution.explain.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.explain.result.ExplainPlanResult;
import com.dbn.execution.explain.result.ui.ExplainPlanResultForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class ExplainPlanResultExpandAllAction extends AbstractExplainPlanResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ExplainPlanResult explainPlanResult) {
        ExplainPlanResultForm resultForm = explainPlanResult.getForm();
        if (isValid(resultForm)) {
            resultForm.expandAllNodes();
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ExplainPlanResult target) {
        presentation.setText("Expand All");
        presentation.setIcon(Icons.ACTION_EXPAND_ALL);
    }
}
