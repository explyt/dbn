package com.dbn.execution.statement.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecutionResultSettingsAction extends AbstractExecutionResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull StatementExecutionCursorResult executionResult) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        settingsManager.openProjectSettings(ConfigId.EXECUTION_ENGINE);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable StatementExecutionCursorResult target) {
        presentation.setText("Settings");
        presentation.setIcon(Icons.EXEC_RESULT_OPTIONS);
    }
}
