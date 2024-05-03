package com.dbn.execution.method.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.ExecutionStatus;
import com.dbn.execution.method.MethodExecutionManager;
import com.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodExecutionStartAction extends AbstractMethodExecutionResultAction {

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull MethodExecutionResult executionResult) {

        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        executionManager.execute(executionResult.getExecutionInput());
    }

    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable MethodExecutionResult target) {


        boolean enabled = target != null &&
                !target.getDebuggerType().isDebug() &&
                target.getExecutionContext().isNot(ExecutionStatus.EXECUTING);

        presentation.setEnabled(enabled);
        presentation.setText("Execute Again");
        presentation.setIcon(Icons.METHOD_EXECUTION_RERUN);
    }
}