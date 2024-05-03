package com.dbn.browser.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.connection.DatabaseType;
import com.dbn.connection.config.ConnectionConfigType;
import com.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConnectionCreateAction extends ProjectAction {
    private final DatabaseType databaseType;

    ConnectionCreateAction(@Nullable DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText(databaseType == null ? "Custom..." : databaseType.getName());
        presentation.setIcon(databaseType == null ? null : databaseType.getIcon());
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project) {

        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);

        DatabaseType databaseType = this.databaseType;
        ConnectionConfigType configType = ConnectionConfigType.BASIC;
        if (databaseType == null) {
            configType = ConnectionConfigType.CUSTOM;
            databaseType = DatabaseType.GENERIC;
        }
        settingsManager.createConnection(databaseType, configType);
    }
}
