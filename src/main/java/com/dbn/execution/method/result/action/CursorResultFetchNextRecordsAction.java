package com.dbn.execution.method.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.common.thread.Progress;
import com.dbn.common.util.Messages;
import com.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dbn.data.model.resultSet.ResultSetDataModel;
import com.dbn.execution.common.options.ExecutionEngineSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public class CursorResultFetchNextRecordsAction extends MethodExecutionCursorResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        if (isNotValid(resultSetTable)) return;

        ResultSetDataModel model = resultSetTable.getModel();
        Progress.prompt(project, model, false,
                "Loading cursor result",
                "Loading method execution cursor result",
                progress -> {
                    try {
                        if (!model.isResultSetExhausted()) {
                            ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(project);
                            int fetchBlockSize = settings.getStatementExecutionSettings().getResultSetFetchBlockSize();

                            model.fetchNextRecords(fetchBlockSize, false);
                        }

                    } catch (SQLException ex) {
                        conditionallyLog(ex);
                        Messages.showErrorDialog(project, "Could not perform operation.", ex);
                    }

                });
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        super.update(e, project);
        ResultSetTable resultSetTable = getResultSetTable(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Fetch Next Records");
        presentation.setIcon(Icons.EXEC_RESULT_RESUME);

        if (resultSetTable != null) {
            ResultSetDataModel model = resultSetTable.getModel();
            boolean enabled = !model.isResultSetExhausted();
            presentation.setEnabled(enabled);
        } else {
            presentation.setEnabled(false);
        }
    }
}
