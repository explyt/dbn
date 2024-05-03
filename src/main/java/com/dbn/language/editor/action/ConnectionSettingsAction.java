package com.dbn.language.editor.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConnectionSettingsAction extends ProjectAction {

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Setup Connections...");
        presentation.setIcon(Icons.ACTION_EDIT);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        settingsManager.openProjectSettings(ConfigId.CONNECTIONS);
    }
}
