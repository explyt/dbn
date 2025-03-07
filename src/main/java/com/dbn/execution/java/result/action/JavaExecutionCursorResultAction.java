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

package com.dbn.execution.java.result.action;

import com.dbn.common.action.DataKeys;
import com.dbn.common.action.ProjectAction;
import com.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dbn.execution.java.result.ui.JavaExecutionCursorResultForm;
import com.dbn.object.DBJavaParameter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JavaExecutionCursorResultAction extends ProjectAction {
    @Nullable
    ResultSetTable getResultSetTable(AnActionEvent e) {
        JavaExecutionCursorResultForm cursorResultForm = getCursorResultForm(e);
        return cursorResultForm == null ? null : cursorResultForm.getTable();
    }

    @Nullable
    JavaExecutionCursorResultForm getCursorResultForm(AnActionEvent e) {
        return e.getData(DataKeys.JAVA_EXECUTION_CURSOR_RESULT_FORM);
    }

    @Nullable
    DBJavaParameter getMethodArgument(AnActionEvent e) {
        return e.getData(DataKeys.JAVA_EXECUTION_ARGUMENT);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        ResultSetTable resultSetTable = getResultSetTable(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(resultSetTable != null);
    }
}
