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

package com.dbn.assistant.chat.window.action;

import com.dbn.assistant.DatabaseAssistantManager;
import com.dbn.assistant.chat.window.ui.ChatBoxForm;
import com.dbn.assistant.profile.wizard.ProfileEditionWizard;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.object.DBAIProfile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Action for opening the AI-assistant profile setup wizard for creating a new profile
 *
 * @author Dan Cioca (Oracle)
 */
public class ProfileCreateAction extends AbstractChatBoxAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ChatBoxForm chatBox = getChatBox(e);
        if (chatBox == null) return;

        ConnectionHandler connection = chatBox.getConnection();
        ConnectionId connectionId = connection.getConnectionId();
        Set<String> profileNames = getProfileNames(project, connectionId);

        ProfileEditionWizard.showWizard(connection, null, profileNames, null);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("New Profile...");
    }

    public Set<String> getProfileNames(Project project, ConnectionId connectionId) {
        DatabaseAssistantManager manager = DatabaseAssistantManager.getInstance(project);
        List<DBAIProfile> profiles = manager.getProfiles(connectionId);

        return profiles.stream().map(p -> p.getName()).collect(Collectors.toSet());
    }
}
