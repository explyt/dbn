package com.dbn.execution.statement.result.action;

import com.dbn.common.action.ContextAction;
import com.dbn.common.action.DataKeys;
import com.dbn.execution.ExecutionManager;
import com.dbn.execution.ExecutionResult;
import com.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class AbstractExecutionResultAction extends ContextAction<StatementExecutionCursorResult> {

    @Nullable
    protected StatementExecutionCursorResult getTarget(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult result = e.getData(DataKeys.STATEMENT_EXECUTION_CURSOR_RESULT);
        if (result == null) {
            Project project = e.getProject();
            if (project != null) {
                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                ExecutionResult executionResult = executionManager.getSelectedExecutionResult();
                if (executionResult instanceof StatementExecutionCursorResult) {
                    return (StatementExecutionCursorResult) executionResult;
                }
            }
        }
        return result;
    }
}
