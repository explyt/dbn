package com.dbn.execution.statement.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class ExecutionResultRerunAction extends AbstractExecutionResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull StatementExecutionCursorResult executionResult) {
        executionResult.reload();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable StatementExecutionCursorResult executionResult) {
        boolean enabled = false;
        if (isValid(executionResult)) {
            ResultSetTable resultTable = executionResult.getResultTable();
            if (isValid(resultTable)) {
                enabled = !resultTable.isLoading();
            }
        }

        presentation.setEnabled(enabled);
        presentation.setText("Rerun Statement");
        presentation.setIcon(Icons.EXEC_RESULT_RERUN);
    }
}
