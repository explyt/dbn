package com.dbn.diagnostics.action;

import com.dbn.common.icon.Icons;
import com.dbn.diagnostics.DiagnosticsManager;
import com.dbn.diagnostics.data.DiagnosticCategory;
import com.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParserDiagnosticsCloseAction extends AbstractParserDiagnosticsAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ParserDiagnosticsForm form) {
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(project);
        diagnosticsManager.closeDiagnosticsConsole(DiagnosticCategory.PARSER);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ParserDiagnosticsForm target) {
        presentation.setText("Close");
        presentation.setIcon(Icons.ACTION_CLOSE);
    }
}
