package com.dbn.diagnostics.action;

import com.dbn.common.icon.Icons;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.thread.Progress;
import com.dbn.diagnostics.ParserDiagnosticsManager;
import com.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParserDiagnosticsRunAction extends AbstractParserDiagnosticsAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ParserDiagnosticsForm form) {
        Progress.prompt(project, null, true,
                "Running diagnostics",
                "Running parser diagnostics", progress -> {
            progress.setIndeterminate(false);
            ParserDiagnosticsManager manager = getManager(project);
            ParserDiagnosticsResult result = manager.runParserDiagnostics(progress);
            Dispatch.run(() -> manager.openParserDiagnostics(result));
        });
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ParserDiagnosticsForm form) {
        ParserDiagnosticsManager manager = getManager(project);
        boolean enabled = !manager.isRunning() && !manager.hasDraftResults();

        presentation.setText("Run Diagnostics");
        presentation.setIcon(Icons.ACTION_EXECUTE);
        presentation.setEnabled(enabled);
    }
}
