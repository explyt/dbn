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

package com.dbn.assistant;

import com.dbn.DatabaseNavigator;
import com.dbn.assistant.provider.AIProvider;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.thread.Progress;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.Priority.HIGH;
import static com.dbn.common.component.Components.projectService;
import static com.dbn.common.util.Messages.showErrorDialog;
import static com.dbn.common.util.Messages.showInfoDialog;
import static com.dbn.nls.NlsResources.txt;

@Slf4j
@State(
        name = AssistantPrerequisiteManager.COMPONENT_NAME,
        storages = @Storage(DatabaseNavigator.STORAGE_FILE))
public class AssistantPrerequisiteManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.AssistantPrerequisiteManager";

    private AssistantPrerequisiteManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static AssistantPrerequisiteManager getInstance(@NotNull Project project) {
        return projectService(project, AssistantPrerequisiteManager.class);
    }

    public void grantNetworkAccess(ConnectionHandler connection, AIProvider provider, String command) {
        Project project = connection.getProject();

        String host = provider.getHost();
        String user = connection.getUserName();
        String title = txt("prc.assistant.title.GrantingAccess");
        String text = txt("prc.assistant.text.GrantingNetworkAccess", host, user);

        Progress.modal(project, connection, false, title, text, progress -> {
            try {
                DatabaseInterfaceInvoker.execute(HIGH, title, text, project, connection.getConnectionId(),
                        c -> connection.getAssistantInterface().grantACLRights(c, command));

                showInfoDialog(project, txt("msg.assistant.title.AccessGranted"), txt("msg.assistant.info.NetworkAccessGranted", host, user));
            } catch (Throwable e) {
                Diagnostics.conditionallyLog(e);
                showErrorDialog(project, txt("msg.assistant.title.AccessGrantFailed"), txt("msg.assistant.error.NetworkAccessGrantFailed", host, user, e.getMessage()));
            }
        });
    }

    public void grantExecutionPrivileges(ConnectionHandler connection, String user) {
        String title = txt("prc.assistant.title.GrantingPrivileges");
        String text = txt("prc.assistant.text.GrantingExecutionPrivileges", user);

        Project project = getProject();
        Progress.modal(project, connection, false, title, text, progress -> {
            try {
                DatabaseInterfaceInvoker.execute(HIGH, title, text, project, connection.getConnectionId(),
                        c -> connection.getAssistantInterface().grantPrivilege(c, user));

                showInfoDialog(project, txt("msg.assistant.title.PrivilegesGranted"), txt("msg.assistant.info.ExecutionPrivilegesGranted", user));
            } catch (Throwable e) {
                Diagnostics.conditionallyLog(e);
                showErrorDialog(project, txt("msg.assistant.title.PrivilegesGrantFailed"), txt("msg.assistant.error.ExecutionPrivilegesGrantFailed", user, e.getMessage()));
            }
        });
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Override
    public Element getComponentState() {
        return null;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {

    }
}
