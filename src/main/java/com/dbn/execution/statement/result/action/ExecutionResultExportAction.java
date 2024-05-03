package com.dbn.execution.statement.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Dialogs;
import com.dbn.data.export.ui.ExportDataDialog;
import com.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isNotValid;

public class ExecutionResultExportAction extends AbstractExecutionResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull StatementExecutionCursorResult executionResult) {
        ResultSetTable resultTable = executionResult.getResultTable();
        if (isNotValid(resultTable)) return;

        Dialogs.show(() -> new ExportDataDialog(resultTable, executionResult));
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable StatementExecutionCursorResult target) {
        presentation.setText("Export Data");
        presentation.setIcon(Icons.DATA_EXPORT);
    }
}
