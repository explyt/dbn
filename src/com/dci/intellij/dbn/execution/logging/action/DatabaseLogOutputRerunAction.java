package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DatabaseLogOutputRerunAction extends AbstractDatabaseLogOutputAction {
    public DatabaseLogOutputRerunAction() {
        super("Rerun Script", Icons.STMT_EXECUTION_RERUN);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = getEventProject(e);
        final DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        if (project != null && loggingResult != null && !loggingResult.isDisposed()) {
            LogOutputContext context = loggingResult.getContext();
            ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
            scriptExecutionManager.executeScript(context.getSourceFile());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Rerun Script");

        DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        LogOutputContext context = loggingResult == null ? null : loggingResult.getContext();
        boolean enabled = context != null && !context.isActive();
        presentation.setEnabled(enabled);

    }
}
