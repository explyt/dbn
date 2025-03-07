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
import com.dbn.assistant.state.AssistantState;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.feature.FeatureAvailability;
import com.dbn.common.feature.FeatureAvailabilityInfo;
import com.dbn.common.listener.DBNFileEditorManagerListener;
import com.dbn.common.thread.Progress;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.mapping.FileConnectionContextListener;
import com.dbn.database.DatabaseFeature;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dbn.common.Priority.HIGHEST;
import static com.dbn.common.component.Components.projectService;
import static com.dbn.common.feature.FeatureAvailability.AVAILABLE;
import static com.dbn.common.feature.FeatureAvailability.UNAVAILABLE;
import static com.dbn.common.feature.FeatureAvailability.UNCERTAIN;
import static com.dbn.common.util.ContextLookup.getConnectionId;
import static com.dbn.nls.NlsResources.txt;

@Slf4j
@State(
        name = AssistantInitializationManager.COMPONENT_NAME,
        storages = @Storage(DatabaseNavigator.STORAGE_FILE))
public class AssistantInitializationManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.AssistantInitializationManager";

    private AssistantInitializationManager(Project project) {
        super(project, COMPONENT_NAME);

        ProjectEvents.subscribe(project, this,
                FileEditorManagerListener.FILE_EDITOR_MANAGER,
                fileEditorManagerListener());

        ProjectEvents.subscribe(project, this,
                FileConnectionContextListener.TOPIC,
                createConnectionContextListener());
    }

    public static AssistantInitializationManager getInstance(@NotNull Project project) {
        return projectService(project, AssistantInitializationManager.class);
    }

    private FileConnectionContextListener createConnectionContextListener() {
        return new FileConnectionContextListener() {
            @Override
            public void connectionChanged(Project project, VirtualFile file, ConnectionHandler connection) {
                if (connection == null) return;
                initialize(connection.getConnectionId());
            }
        };
    }

    private FileEditorManagerListener fileEditorManagerListener() {
        return new DBNFileEditorManagerListener() {
            @Override
            public void whenSelectionChanged(FileEditorManagerEvent event) {
                FileEditor editor = event.getNewEditor();
                ConnectionId connectionId = getConnectionId(getProject(), editor);
                if (connectionId == null) return;

                initialize(connectionId);
            }
        };
    }

    private AssistantState getAssistantState(ConnectionId connectionId) {
        Project project = getProject();
        DatabaseAssistantManager assistantManager = DatabaseAssistantManager.getInstance(project);
        return assistantManager.getAssistantState(connectionId);
    }

    private void initialize(ConnectionId connectionId) {
        AssistantState assistantState = getAssistantState(connectionId);
        if (assistantState.getAvailability() != UNCERTAIN) return; // already initialized

        Project project = getProject();
        ConnectionHandler connection = ConnectionHandler.get(connectionId);

        Progress.background(project, connection, false,
                txt("prc.assistant.title.CheckingFeatureAvailability"),
                txt("prc.assistant.text.CheckingFeatureAvailability"),
                p -> verifyAssistantAvailability(connectionId));
    }


    public FeatureAvailabilityInfo verifyAssistantAvailability(ConnectionId connectionId) {
        AssistantState assistantState = getAssistantState(connectionId);
        FeatureAvailability availability = assistantState.getAvailability();

        if (availability != UNCERTAIN) return new FeatureAvailabilityInfo(availability);
        synchronized (this) {
            availability = assistantState.getAvailability();
            if (availability != UNCERTAIN) return new FeatureAvailabilityInfo(availability);
            return evaluateAssistantAvailability(assistantState);
        }
    }

    /**
     * Verifies the availability of the AI Assistant if not already known and captured in the {@link AssistantState}
     * @return an {@link FeatureAvailabilityInfo} object
     */
    private FeatureAvailabilityInfo evaluateAssistantAvailability(AssistantState assistantState) {
        DatabaseAssistantType assistantType = assistantState.getAssistantType();
        FeatureAvailability availability = assistantState.getAvailability();
        String availabilityMessage = null;


        ConnectionId connectionId = assistantState.getConnectionId();
        ConnectionHandler connection = ConnectionHandler.ensure(connectionId);
        if (connection.isVirtual()) {
            availability = UNAVAILABLE;

        } else if (!DatabaseFeature.AI_ASSISTANT.isSupported(connection)) {
            // known already to bot be supported by the given database type
            availability = UNAVAILABLE;
            
        } else {
            // perform deep verification by accessing the database
            try {
                boolean available = checkAvailability(connection);
                assistantType = resolveAssistantType(connection);

                availability = available ? AVAILABLE : UNAVAILABLE;
            } catch (Throwable e) {
                // availability remains uncertain at this stage as it could bot be verified against the database
                Diagnostics.conditionallyLog(e);
                availabilityMessage = e.getMessage();
            }
        }

        assistantState.setAssistantType(assistantType);
        assistantState.setAvailability(availability);

        return new FeatureAvailabilityInfo(availability, availabilityMessage);
    }


    private static boolean checkAvailability(ConnectionHandler connection) throws SQLException {
        return DatabaseInterfaceInvoker.load(HIGHEST,
                txt("prc.assistant.title.LoadingMetadata"),
                txt("prc.assistant.text.CheckingFeatureSupport"),
                connection.getProject(),
                connection.getConnectionId(),
                conn -> connection.getAssistantInterface().isAssistantFeatureSupported(conn));
    }

    private static DatabaseAssistantType resolveAssistantType(ConnectionHandler connection) throws SQLException {
        return DatabaseInterfaceInvoker.load(HIGHEST,
                txt("prc.assistant.title.LoadingMetadata"),
                txt("prc.assistant.text.ResolvingAssistantType"),
                connection.getProject(),
                connection.getConnectionId(),
                conn -> connection.getAssistantInterface().getAssistantType(conn));
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
