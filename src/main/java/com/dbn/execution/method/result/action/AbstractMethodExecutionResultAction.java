package com.dbn.execution.method.result.action;

import com.dbn.common.action.ContextAction;
import com.dbn.common.action.DataKeys;
import com.dbn.execution.ExecutionManager;
import com.dbn.execution.ExecutionResult;
import com.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMethodExecutionResultAction extends ContextAction<MethodExecutionResult> {

    protected MethodExecutionResult getTarget(@NotNull AnActionEvent e) {
        MethodExecutionResult result = e.getData(DataKeys.METHOD_EXECUTION_RESULT);
        if (result != null) return result;

        Project project = e.getProject();
        if (project == null) return result;

        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        ExecutionResult executionResult = executionManager.getSelectedExecutionResult();
        if (executionResult instanceof MethodExecutionResult) {
            return (MethodExecutionResult) executionResult;
        }

        return null;
    }
}
