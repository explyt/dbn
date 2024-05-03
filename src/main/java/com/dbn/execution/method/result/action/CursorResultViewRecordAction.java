package com.dbn.execution.method.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CursorResultViewRecordAction extends MethodExecutionCursorResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        if (resultSetTable == null) return;

        resultSetTable.showRecordViewDialog();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        boolean enabled = resultSetTable != null && resultSetTable.getSelectedColumn() > -1;

        Presentation presentation = e.getPresentation();
        presentation.setText("View Record");
        presentation.setIcon(Icons.EXEC_RESULT_VIEW_RECORD);
        presentation.setEnabled(enabled);
    }
}
