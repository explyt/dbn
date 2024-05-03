package com.dbn.diagnostics.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.common.util.Messages;
import com.dbn.diagnostics.Diagnostics;
import com.dbn.diagnostics.DiagnosticsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class DeveloperModeAction extends ProjectAction {

    private static void openDiagnosticSettings(Project project) {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(project);
        diagnosticsManager.openDiagnosticsSettings();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setText("Developer Mode...");
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        boolean developerMode = Diagnostics.isDeveloperMode();
        if (developerMode) {
            String remainingTime = Diagnostics.getDeveloperMode().getRemainingTime();
            Messages.showWarningDialog(project,
                    "Developer Mode (ACTIVE)",
                    "Developer Mode is currently ACTIVE.\n" +
                            "It will be automatically disabled after " + remainingTime,
                    new String[]{"Disable Now", "Cancel", "Open Settings..."}, 0,
                    option -> actionPerformed(project, option, false));
        } else {
            int timeoutMinutes = Diagnostics.getDeveloperMode().getTimeout();
            Messages.showInfoDialog(project,
                    "Developer Mode (INACTIVE)",
                    "Developer Mode is currently INACTIVE\n" +
                            "Do NOT enable Developer Mode unless explicitly instructed to do so by the DBN plugin development team\n\n" +
                            "If enabling, it will be automatically disabled after " + timeoutMinutes + " minutes",
                    new String[]{"Enable", "Cancel", "Open Settings..."}, 0,
                    option -> actionPerformed(project, option, true));
        }
    }

    private static void actionPerformed(@NotNull Project project, int option, boolean enabled) {
        if (option == 0) {
            Diagnostics.getDeveloperMode().setEnabled(enabled);
            if (enabled && !Diagnostics.hasEnabledFeatures()) {
                openDiagnosticSettings(project);
            }
        } else if (option == 2) {
            openDiagnosticSettings(project);
        }
    }
}
