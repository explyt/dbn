package com.dbn.execution.statement.result.action;

import com.dbn.common.dispose.Failsafe;
import com.dbn.common.icon.Icons;
import com.dbn.execution.common.ui.StatementViewerPopup;
import com.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ExecutionResultViewStatementAction extends AbstractExecutionResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull StatementExecutionCursorResult executionResult) {
        StatementViewerPopup statementViewer = new StatementViewerPopup(executionResult);
        statementViewer.show((Component) Failsafe.nn(e.getInputEvent()).getSource());
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable StatementExecutionCursorResult target) {
        presentation.setText("View SQL Statement");
        presentation.setIcon(Icons.EXEC_RESULT_VIEW_STATEMENT);
    }

}
