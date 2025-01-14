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
 *
 */

package com.dbn.execution.java.action;

import com.dbn.common.icon.Icons;
import com.dbn.debugger.DBDebuggerType;
import com.dbn.execution.java.JavaExecutionManager;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBMethod;
import com.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaRunAction extends AnObjectAction<DBJavaMethod> {
    private final boolean listElement;
    public JavaRunAction(DBJavaMethod method, boolean listElement) {
        super(method);
        this.listElement = listElement;
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DBJavaMethod object) {

        JavaExecutionManager executionManager = JavaExecutionManager.getInstance(project);
        executionManager.startMethodExecution(object, DBDebuggerType.NONE);
    }

    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable DBJavaMethod target) {
        if (listElement) {
            super.update(e, presentation, project, target);
        } else {
            presentation.setText("Run...");
            presentation.setIcon(Icons.METHOD_EXECUTION_RUN);
        }
    }
}
