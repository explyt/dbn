package com.dbn.execution.method.result.action;

import com.dbn.common.action.DataKeys;
import com.dbn.common.action.ProjectAction;
import com.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dbn.execution.method.result.ui.MethodExecutionCursorResultForm;
import com.dbn.object.DBArgument;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class MethodExecutionCursorResultAction extends ProjectAction {
    @Nullable
    ResultSetTable getResultSetTable(AnActionEvent e) {
        MethodExecutionCursorResultForm cursorResultForm = getCursorResultForm(e);
        return cursorResultForm == null ? null : cursorResultForm.getTable();
    }

    @Nullable
    MethodExecutionCursorResultForm getCursorResultForm(AnActionEvent e) {
        return e.getData(DataKeys.METHOD_EXECUTION_CURSOR_RESULT_FORM);
    }

    @Nullable
    DBArgument getMethodArgument(AnActionEvent e) {
        return e.getData(DataKeys.METHOD_EXECUTION_ARGUMENT);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(resultSetTable != null);
    }
}
