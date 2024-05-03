package com.dbn.execution.explain.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.ExecutionManager;
import com.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExplainPlanResultCloseAction extends AbstractExplainPlanResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ExplainPlanResult explainPlanResult) {
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.removeResultTab(explainPlanResult);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ExplainPlanResult target) {
        presentation.setText("Close");
        presentation.setIcon(Icons.EXEC_RESULT_CLOSE);
    }
}
