package com.dbn.execution.logging.action;

import com.dbn.common.component.ComponentBase;
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.dbn.execution.logging.DatabaseLoggingResult;
import com.dbn.execution.logging.LogOutputContext;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.util.Conditional.when;

public class DatabaseLogOutputKillAction extends AbstractDatabaseLoggingAction implements ComponentBase {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatabaseLoggingResult loggingResult) {
        LogOutputContext context = loggingResult.getContext();
        if (context.isActive()) {
            Messages.showQuestionDialog(
                    project,
                    "Kill process",
                    "This will interrupt the script execution process. \nAre you sure you want to continue?",
                    Messages.OPTIONS_YES_NO, 0,
                    option -> when(option == 0, () -> context.stop()));

        } else {
            context.stop();
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DatabaseLoggingResult loggingResult) {
        presentation.setText("Kill Process");
        presentation.setIcon(Icons.KILL_PROCESS);

        LogOutputContext context = loggingResult == null ? null : loggingResult.getContext();
        boolean enabled = context != null && context.isActive();
        boolean visible = context != null && context.getProcess() != null;
        presentation.setEnabled(enabled);
        presentation.setVisible(visible);

    }
}
