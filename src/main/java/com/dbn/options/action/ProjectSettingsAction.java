package com.dbn.options.action;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.common.action.ProjectAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ProjectSettingsAction extends ProjectAction {
    private final ConfigId configId;

    ProjectSettingsAction(ConfigId configId) {
        this.configId = configId;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);

        if (configId == ConfigId.CONNECTIONS) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            ConnectionHandler activeConnection = browserManager.getActiveConnection();
            ConnectionId connectionId = activeConnection == null ? null : activeConnection.getConnectionId();
            settingsManager.openConnectionSettings(connectionId);
        }
        else {
            settingsManager.openProjectSettings(configId);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText(configId.getName() + "...");
    /*
            presentation.setIcon(Icons.ACTION_SETTINGS);
            presentation.setText("Settings");
    */
    }
}
