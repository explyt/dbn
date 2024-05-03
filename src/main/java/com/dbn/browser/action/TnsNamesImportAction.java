package com.dbn.browser.action;


import com.dbn.common.action.ProjectAction;
import com.dbn.connection.config.tns.TnsImportService;
import com.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TnsNamesImportAction extends ProjectAction {
    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setText("Import TNS Names");
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        TnsImportService importService = TnsImportService.getInstance();
        importService.importTnsNames(project, d -> settingsManager.createConnections(d));
    }
}
