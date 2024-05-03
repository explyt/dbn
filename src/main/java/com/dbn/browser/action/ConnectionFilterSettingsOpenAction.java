package com.dbn.browser.action;

import com.dbn.browser.DatabaseBrowserManager;
import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Dialogs;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.config.ui.ConnectionFilterSettingsDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConnectionFilterSettingsOpenAction extends ProjectAction {
    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Object Filters...");
        presentation.setIcon(Icons.DATASET_FILTER);

        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        ConnectionHandler activeConnection = browserManager.getActiveConnection();
        presentation.setEnabled(activeConnection != null);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        ConnectionHandler activeConnection = browserManager.getActiveConnection();
        if (activeConnection == null) return;

        Dialogs.show(() -> new ConnectionFilterSettingsDialog(activeConnection));
    }
}
