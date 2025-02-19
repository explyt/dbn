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

package com.dbn.menu.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.connection.ConnectionBundle;
import com.dbn.connection.ConnectionManager;
import com.dbn.execution.java.JavaExecutionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;


public class JavaExecutionHistoryAction extends ProjectAction {

	@Override
	protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
		ConnectionManager connectionManager = ConnectionManager.getInstance(project);
		ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
		if (connectionBundle.isEmpty()) {
			connectionManager.promptMissingConnection();
			return;
		}

		JavaExecutionManager executionManager = JavaExecutionManager.getInstance(project);
		executionManager.showExecutionHistoryDialog(null, true, false, false, null);
	}
}

