package com.dbn.execution.method.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.ExecutionManager;
import com.dbn.execution.method.result.MethodExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecutionResultCloseAction extends AbstractMethodExecutionResultAction {

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull MethodExecutionResult executionResult) {

        ExecutionManager.getInstance(project).removeResultTab(executionResult);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable MethodExecutionResult target) {
        presentation.setText("Close");
        presentation.setIcon(Icons.EXEC_RESULT_CLOSE);
    }
}
