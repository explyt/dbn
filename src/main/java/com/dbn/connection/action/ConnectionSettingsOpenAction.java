package com.dbn.connection.action;

import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConnectionSettingsOpenAction extends AbstractConnectionAction {

    ConnectionSettingsOpenAction(ConnectionHandler connection) {
        super("Settings", "Connection settings", Icons.ACTION_OPTIONS, connection);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        settingsManager.openConnectionSettings(connection.getConnectionId());
    }
}
