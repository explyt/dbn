package com.dbn.connection.config.action;

import com.dbn.connection.DatabaseType;
import com.dbn.connection.config.ConnectionConfigType;
import com.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectionCreateAction extends ConnectionSettingsAction {
    private final DatabaseType databaseType;

    ConnectionCreateAction(@Nullable DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ConnectionBundleSettingsForm target) {

        DatabaseType databaseType = this.databaseType;
        ConnectionConfigType configType = ConnectionConfigType.BASIC;
        if (databaseType == null) {
            configType = ConnectionConfigType.CUSTOM;
            databaseType = DatabaseType.GENERIC;
        }

        target.createNewConnection(databaseType, configType);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionBundleSettingsForm target) {
        presentation.setText(databaseType == null ? "Custom..." : databaseType.getName());
        presentation.setIcon(databaseType == null ? null : databaseType.getIcon());
    }
}


