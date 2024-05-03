package com.dbn.connection.config.action;

import com.dbn.common.icon.Icons;
import com.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectionRemoveAction extends ConnectionSettingsAction {

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ConnectionBundleSettingsForm target) {

        target.removeSelectedConnections();
    }

    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable ConnectionBundleSettingsForm target) {

        int length = target == null ? 0 : target.getSelectionSize();
        presentation.setEnabled(length > 0);
        presentation.setText(length == 1 ? "Remove Connections" : "Remove Connection");
        presentation.setIcon(Icons.ACTION_REMOVE);
    }
}
