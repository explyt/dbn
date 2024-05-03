package com.dbn.diagnostics.action;

import com.dbn.common.icon.Icons;
import com.dbn.diagnostics.ParserDiagnosticsManager;
import com.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParserDiagnosticsSaveAction extends AbstractParserDiagnosticsAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ParserDiagnosticsForm form) {
        ParserDiagnosticsManager manager = getManager(project);
        ParserDiagnosticsResult result = form.getSelectedResult();
        if (result != null && result.isDraft()) {
            manager.saveResult(result);
            form.refreshResults();
            form.selectResult(result);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ParserDiagnosticsForm form) {
        presentation.setText("Save Result");
        presentation.setIcon(Icons.ACTION_SAVE);
        if (form != null) {
            ParserDiagnosticsResult result = form.getSelectedResult();
            presentation.setEnabled(result != null && result.isDraft());
        } else {
            presentation.setEnabled(false);
        }

    }
}
