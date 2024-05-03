package com.dbn.execution.explain.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.explain.result.ExplainPlanResult;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExplainPlanResultSettingsAction extends AbstractExplainPlanResultAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ExplainPlanResult explainPlanResult) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        settingsManager.openProjectSettings(ConfigId.EXECUTION_ENGINE);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ExplainPlanResult target) {
        presentation.setText("Settings");
        presentation.setIcon(Icons.EXEC_RESULT_OPTIONS);
    }
}
