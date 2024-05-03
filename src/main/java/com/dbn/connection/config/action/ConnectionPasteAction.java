package com.dbn.connection.config.action;

import com.dbn.common.clipboard.Clipboard;
import com.dbn.common.icon.Icons;
import com.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConnectionPasteAction extends ConnectionSettingsAction {

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ConnectionBundleSettingsForm target) {

            target.pasteConnectionsFromClipboard();
    }

    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable ConnectionBundleSettingsForm target) {

        String clipboardString = Clipboard.getStringContent();
        boolean enabled = clipboardString != null && clipboardString.contains("connection-configurations");

        presentation.setEnabled(enabled);
        presentation.setText("Paste From Clipboard");
        presentation.setIcon(Icons.CONNECTION_PASTE);
    }
}
