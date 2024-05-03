package com.dbn.execution.explain.result.action;

import com.dbn.common.action.ContextAction;
import com.dbn.common.action.DataKeys;
import com.dbn.execution.ExecutionManager;
import com.dbn.execution.ExecutionResult;
import com.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.dispose.Checks.isNotValid;

public abstract class AbstractExplainPlanResultAction extends ContextAction<ExplainPlanResult> {
    protected ExplainPlanResult getTarget(@NotNull AnActionEvent e) {
        ExplainPlanResult result = e.getData(DataKeys.EXPLAIN_PLAN_RESULT);
        if (result != null) return result;

        Project project = e.getProject();
        if (isNotValid(project)) return null;

        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        ExecutionResult executionResult = executionManager.getSelectedExecutionResult();
        if (executionResult instanceof ExplainPlanResult) {
            return (ExplainPlanResult) executionResult;
        }
        return null;
    }
}
