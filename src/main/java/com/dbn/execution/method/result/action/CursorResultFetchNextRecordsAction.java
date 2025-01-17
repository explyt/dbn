/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import static com.dbn.nls.NlsResources.txt;

public class CursorResultFetchNextRecordsAction extends MethodExecutionCursorResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        if (isNotValid(resultSetTable)) return;

        ResultSetDataModel model = resultSetTable.getModel();
        Progress.prompt(project, model, false,
                txt("prc.execution.title.LoadingCursorResult"),
                txt("prc.execution.text.LoadingMethodCursorResult"),
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
        presentation.setText(txt("app.execution.action.FetchNextRecords"));
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
