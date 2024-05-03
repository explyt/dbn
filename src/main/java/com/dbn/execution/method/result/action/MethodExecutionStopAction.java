package com.dbn.execution.method.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.ExecutionStatus;
import com.dbn.execution.method.MethodExecutionContext;
import com.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodExecutionStopAction extends AbstractMethodExecutionResultAction {

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull MethodExecutionResult target) {

        MethodExecutionContext context = target.getExecutionContext();
        context.set(ExecutionStatus.CANCELLED, true);
        context.set(ExecutionStatus.CANCEL_REQUESTED, true);
        ProgressIndicator progress = context.getProgress();
        if (progress != null && !progress.isCanceled()) progress.cancel();

    }

    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable MethodExecutionResult target) {

        boolean enabled = target != null &&
                !target.getDebuggerType().isDebug() &&
                target.getExecutionContext().is(ExecutionStatus.EXECUTING) &&
                target.getExecutionContext().isNot(ExecutionStatus.CANCELLED) &&
                target.getExecutionContext().isNot(ExecutionStatus.CANCEL_REQUESTED);

        presentation.setEnabled(enabled) ;
        presentation.setText("Stop Execution");
        presentation.setIcon(Icons.METHOD_EXECUTION_STOP);
    }
}