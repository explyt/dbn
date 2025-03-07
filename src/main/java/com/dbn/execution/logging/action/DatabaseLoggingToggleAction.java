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

package com.dbn.execution.logging.action;

import com.dbn.common.action.BackgroundUpdate;
import com.dbn.common.action.Lookups;
import com.dbn.common.action.ToggleAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.util.Strings;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.database.DatabaseFeature;
import com.dbn.database.interfaces.DatabaseCompatibilityInterface;
import com.dbn.debugger.DatabaseDebuggerManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.connection.ConnectionHandler.isLiveConnection;
import static com.dbn.nls.NlsResources.txt;

@BackgroundUpdate
public class DatabaseLoggingToggleAction extends ToggleAction implements DumbAware {

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        ConnectionHandler activeConnection = getConnection(e);
        return activeConnection != null && activeConnection.isLoggingEnabled();
    }

    @Nullable
    private static ConnectionHandler getConnection(AnActionEvent e) {
        Project project = Lookups.getProject(e);
        VirtualFile virtualFile = Lookups.getVirtualFile(e);
        if (project == null || virtualFile == null) return null;

        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        ConnectionHandler connection = contextManager.getConnection(virtualFile);
        if (isLiveConnection(connection)) return connection;

        return null;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean selected) {
        ConnectionHandler connection = getConnection(e);
        if (connection != null) connection.setLoggingEnabled(selected);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        ConnectionHandler connection = getConnection(e);
        Presentation presentation = e.getPresentation();

        boolean visible = false;
        String name = txt("app.execution.action.DatabaseLogging");
        if (connection != null) {
            boolean supportsLogging = DatabaseFeature.DATABASE_LOGGING.isSupported(connection);
            if (supportsLogging && isVisible(e)) {
                visible = true;
                DatabaseCompatibilityInterface compatibility = connection.getCompatibilityInterface();
                String databaseLogName = compatibility.getDatabaseLogName();
                if (Strings.isNotEmpty(databaseLogName)) {
                    name = name + " (" + databaseLogName + ")";
                }
            }
        }
        presentation.setText(name);
        presentation.setVisible(visible);
        presentation.setIcon(Icons.ACTION_TOGGLE_LOGGING);
    }

    public static boolean isVisible(AnActionEvent e) {
        VirtualFile virtualFile = Lookups.getVirtualFile(e);
        return !DatabaseDebuggerManager.isDebugConsole(virtualFile);
    }
}