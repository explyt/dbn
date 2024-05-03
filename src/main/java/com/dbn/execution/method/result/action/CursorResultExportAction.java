package com.dbn.execution.method.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Dialogs;
import com.dbn.data.export.ui.ExportDataDialog;
import com.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dbn.object.DBArgument;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CursorResultExportAction extends MethodExecutionCursorResultAction {
    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Export Data");
        presentation.setIcon(Icons.DATA_EXPORT);
    }
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ResultSetTable<?> resultSetTable = getResultSetTable(e);
        if (resultSetTable == null) return;

        DBArgument methodArgument = getMethodArgument(e);
        if (methodArgument == null) return;

        Dialogs.show(() -> new ExportDataDialog(resultSetTable, methodArgument));
    }
}
