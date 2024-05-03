package com.dbn.connection.config.action;

import com.dbn.common.icon.Icons;
import com.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectionsCopyAction extends ConnectionSettingsAction {

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ConnectionBundleSettingsForm target) {

        target.copyConnectionsToClipboard();
    }


    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable ConnectionBundleSettingsForm target) {

        boolean enabled = target != null && target.getSelectionSize() > 0;

        presentation.setEnabled(enabled);
        presentation.setText("Copy to Clipboard");
        presentation.setIcon(Icons.CONNECTION_COPY);
    }
}
