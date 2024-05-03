package com.dbn.execution.logging.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.logging.DatabaseLoggingResult;
import com.dbn.execution.logging.LogOutputContext;
import com.dbn.execution.script.ScriptExecutionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseLogOutputRerunAction extends AbstractDatabaseLoggingAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatabaseLoggingResult loggingResult) {
        LogOutputContext context = loggingResult.getContext();
        VirtualFile sourceFile = context.getSourceFile();
        if (sourceFile != null) {
            ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
            scriptExecutionManager.executeScript(sourceFile);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatabaseLoggingResult loggingResult) {
        presentation.setText("Rerun Script");
        presentation.setIcon(Icons.STMT_EXECUTION_RERUN);

        LogOutputContext context = loggingResult == null ? null : loggingResult.getContext();
        boolean enabled = context != null && context.getSourceFile() != null && !context.isActive();
        presentation.setEnabled(enabled);

    }
}
