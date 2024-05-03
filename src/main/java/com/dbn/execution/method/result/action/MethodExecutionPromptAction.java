package com.dbn.execution.method.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.debugger.DBDebuggerType;
import com.dbn.execution.ExecutionStatus;
import com.dbn.execution.method.MethodExecutionInput;
import com.dbn.execution.method.MethodExecutionManager;
import com.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodExecutionPromptAction extends AbstractMethodExecutionResultAction {

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull MethodExecutionResult executionResult) {

        MethodExecutionInput executionInput = executionResult.getExecutionInput();
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        executionManager.startMethodExecution(executionInput, DBDebuggerType.NONE);
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
        presentation.setText("Open Execution Dialog");
        presentation.setIcon(Icons.METHOD_EXECUTION_DIALOG);
    }
}