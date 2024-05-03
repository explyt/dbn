package com.dbn.connection.config.action;


import com.dbn.connection.config.tns.TnsImportService;
import com.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TnsNamesImportAction extends ConnectionSettingsAction{

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull ConnectionBundleSettingsForm target) {
        TnsImportService importService = TnsImportService.getInstance();
        importService.importTnsNames(project, d -> target.importTnsNames(d));
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ConnectionBundleSettingsForm target) {
        presentation.setText("Import TNS Names");
    }
}
