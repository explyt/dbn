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

package com.dbn.execution.method;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.dispose.Disposer;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.routine.Consumer;
import com.dbn.common.thread.Progress;
import com.dbn.common.util.Commons;
import com.dbn.common.util.Dialogs;
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ConnectionConfigListener;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.DatabaseFeature;
import com.dbn.database.common.execution.MethodExecutionProcessor;
import com.dbn.database.interfaces.DatabaseExecutionInterface;
import com.dbn.debugger.DBDebuggerType;
import com.dbn.execution.ExecutionManager;
import com.dbn.execution.ExecutionStatus;
import com.dbn.execution.common.input.ExecutionVariable;
import com.dbn.execution.common.input.ExecutionVariableHistory;
import com.dbn.execution.method.browser.MethodBrowserSettings;
import com.dbn.execution.method.browser.ui.MethodExecutionBrowserDialog;
import com.dbn.execution.method.history.ui.MethodExecutionHistoryDialog;
import com.dbn.execution.method.ui.MethodExecutionHistory;
import com.dbn.execution.method.ui.MethodExecutionInputDialog;
import com.dbn.object.DBMethod;
import com.dbn.object.DBSchema;
import com.dbn.object.common.ui.ObjectTreeModel;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import lombok.Getter;
import lombok.val;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

import static com.dbn.common.component.Components.projectService;
import static com.dbn.common.dispose.Checks.isValid;
import static com.dbn.common.options.setting.Settings.newElement;
import static com.dbn.common.options.setting.Settings.newStateElement;
import static com.dbn.common.util.Conditional.when;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.dbn.nls.NlsResources.txt;

@State(
    name = MethodExecutionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
@Getter
public class MethodExecutionManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.MethodExecutionManager";

    private final MethodBrowserSettings browserSettings = new MethodBrowserSettings();
    private final MethodExecutionHistory executionHistory = new MethodExecutionHistory(getProject());
    private final ExecutionVariableHistory argumentValuesHistory = new ExecutionVariableHistory();

    private MethodExecutionManager(Project project) {
        super(project, COMPONENT_NAME);
        ProjectEvents.subscribe(project, this, ConnectionConfigListener.TOPIC, connectionConfigListener());
    }

    @NotNull
    private ConnectionConfigListener connectionConfigListener() {
        return new ConnectionConfigListener() {
            @Override
            public void connectionRemoved(ConnectionId connectionId) {
                browserSettings.connectionRemoved(connectionId);
                executionHistory.connectionRemoved(connectionId);
                argumentValuesHistory.connectionRemoved(connectionId);
            }
        };
    }

    public static MethodExecutionManager getInstance(@NotNull Project project) {
        return projectService(project, MethodExecutionManager.class);
    }

    public MethodExecutionInput getExecutionInput(DBMethod method) {
        return executionHistory.getExecutionInput(method);
    }

    @NotNull
    public MethodExecutionInput getExecutionInput(DBObjectRef<DBMethod> methodRef) {
        return executionHistory.getExecutionInput(methodRef, true);
    }

    public void startMethodExecution(@NotNull MethodExecutionInput executionInput, @NotNull DBDebuggerType debuggerType) {
        promptExecutionDialog(executionInput, debuggerType, () -> MethodExecutionManager.this.execute(executionInput));
    }

    public void startMethodExecution(@NotNull DBMethod method, @NotNull DBDebuggerType debuggerType) {
        promptExecutionDialog(method, debuggerType, () -> MethodExecutionManager.this.execute(method));
    }

    private void promptExecutionDialog(@NotNull DBMethod method, @NotNull DBDebuggerType debuggerType, Runnable callback) {
        MethodExecutionInput executionInput = getExecutionInput(method);
        promptExecutionDialog(executionInput, debuggerType, callback);
    }

    public void promptExecutionDialog(MethodExecutionInput executionInput, @NotNull DBDebuggerType debuggerType, Runnable callback) {
        Project project = executionInput.getProject();
        DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();

        ConnectionAction.invoke(txt("msg.execution.title.MethodExecution"), false, executionInput,
                action -> Progress.prompt(project, action, true,
                        txt("prc.execution.title.LoadingMethodDetails"),
                        txt("prc.execution.text.LoadingMethodDetails", methodRef.getQualifiedNameWithType()),
                        progress -> {
                            ConnectionHandler connection = action.getConnection();
                            String methodIdentifier = methodRef.getPath();
                            if (connection.isValid()) {
                                DBMethod method = executionInput.getMethod();
                                if (method == null) {
                                    Messages.showErrorDialog(project,
                                            txt("msg.execution.message.MethodNotFound", methodIdentifier));
                                } else {
                                    // load the arguments and declared types while in background
                                    executionInput.initDatabaseElements();
                                    showInputDialog(executionInput, debuggerType, callback);
                                }
                            } else {
                                Messages.showErrorDialog(project,
                                        txt("msg.execution.message.MethodExecutionConnectivityError", methodIdentifier, connection.getName()));
                            }
                        }));
    }

    private void showInputDialog(@NotNull MethodExecutionInput executionInput, @NotNull DBDebuggerType debuggerType, @NotNull Runnable executor) {
        Dialogs.show(() -> new MethodExecutionInputDialog(executionInput, debuggerType, executor));
    }


    public void showExecutionHistoryDialog(
            MethodExecutionInput selection,
            boolean editable,
            boolean modal,
            boolean debug,
            Consumer<MethodExecutionInput> callback) {

        Project project = getProject();
        Progress.prompt(project, selection, true,
                txt("prc.execution.title.LoadingDataDictionary"),
                txt("prc.execution.text.LoadingMethodExecutionHistory"),
                progress -> {
                    MethodExecutionInput selectedInput = Commons.nvln(selection, executionHistory.getLastSelection());
                    if (selectedInput != null) {
                        // initialize method arguments while in background
                        DBMethod method = selectedInput.getMethod();
                        if (isValid(method)) {
                            method.getArguments();
                        }
                    }

                    if (!progress.isCanceled()) {
                        Dialogs.show(
                                () -> new MethodExecutionHistoryDialog(project, selectedInput, editable, modal, debug),
                                (dialog, exitCode) -> {
                                    if (exitCode != DialogWrapper.OK_EXIT_CODE) return;

                                    MethodExecutionInput newlySelected = dialog.getSelectedExecutionInput();
                                    if (newlySelected == null) return;
                                    if (callback == null) return;

                                    callback.accept(newlySelected);
                                });
                    }
                });
    }

    public void execute(DBMethod method) {
        MethodExecutionInput executionInput = getExecutionInput(method);
        execute(executionInput);
    }

    public void execute(MethodExecutionInput input) {
        cacheArgumentValues(input);
        executionHistory.setSelection(input.getMethodRef());
        DBMethod method = input.getMethod();
        MethodExecutionContext context = input.getExecutionContext();
        context.set(ExecutionStatus.EXECUTING, true);

        if (method == null) {
            DBObjectRef<DBMethod> methodRef = input.getMethodRef();
            String methodIdentifier = methodRef.getQualifiedNameWithType();
            Messages.showErrorDialog(getProject(), txt("msg.execution.message.CannotResolveMethod", methodIdentifier));
        } else {
            Project project = method.getProject();
            ConnectionHandler connection = Failsafe.nn(method.getConnection());
            DatabaseExecutionInterface executionInterface = connection.getInterfaces().getExecutionInterface();
            MethodExecutionProcessor executionProcessor = executionInterface.createExecutionProcessor(method);

            Progress.prompt(project, method, true,
                    txt("prc.execution.title.ExecutingMethod"),
                    txt("prc.execution.text.ExecutingMethod", method.getQualifiedNameWithType()),
                    progress -> {
                try {
                    executionProcessor.execute(input, DBDebuggerType.NONE);
                    if (context.isNot(ExecutionStatus.CANCELLED)) {
                        ExecutionManager executionManager = ExecutionManager.getInstance(project);
                        executionManager.addExecutionResult(input.getExecutionResult());
                        context.set(ExecutionStatus.EXECUTING, false);
                    }

                    context.set(ExecutionStatus.CANCELLED, false);
                } catch (SQLException e) {
                    conditionallyLog(e);
                    context.set(ExecutionStatus.EXECUTING, false);
                    if (context.isNot(ExecutionStatus.CANCELLED)) {
                        Messages.showErrorDialog(project,
                                txt("msg.execution.title.MethodExecutionError"),
                                txt("msg.execution.message.MethodExecutionError", method.getQualifiedNameWithType(), e.getMessage()),
                                new String[]{
                                        txt("msg.shared.button.TryAgain"),
                                        txt("msg.shared.button.Cancel")}, 0,
                                option -> when(option == 0, () ->
                                        startMethodExecution(input, DBDebuggerType.NONE)));
                    }
                }
            });
        }
    }

    private void cacheArgumentValues(MethodExecutionInput input) {
        ConnectionHandler connection = input.getExecutionContext().getTargetConnection();
        if (connection == null) return;

        for (val entry : input.getArgumentValueHistory().entrySet()) {
            ExecutionVariable argumentValue = entry.getValue();

            argumentValuesHistory.cacheVariable(
                    connection.getConnectionId(),
                    argumentValue.getPath(),
                    argumentValue.getValue());
        }
    }

    public void debugExecute(
            @NotNull MethodExecutionInput input,
            @NotNull DBNConnection conn,
            DBDebuggerType debuggerType) throws SQLException {

        DBMethod method = input.getMethod();
        if (method == null) return;

        ConnectionHandler connection = method.getConnection();
        DatabaseExecutionInterface executionInterface = connection.getInterfaces().getExecutionInterface();
        MethodExecutionProcessor executionProcessor = debuggerType == DBDebuggerType.JDWP ?
                executionInterface.createExecutionProcessor(method) :
                executionInterface.createDebugExecutionProcessor(method);

        executionProcessor.execute(input, conn, debuggerType);
        MethodExecutionContext context = input.getExecutionContext();
        if (context.isNot(ExecutionStatus.CANCELLED)) {
            ExecutionManager executionManager = ExecutionManager.getInstance(method.getProject());
            executionManager.addExecutionResult(input.getExecutionResult());
        }
        context.set(ExecutionStatus.CANCELLED, false);
    }

    public void promptMethodBrowserDialog(
            @Nullable MethodExecutionInput executionInput, boolean debug,
            @Nullable Consumer<MethodExecutionInput> callback) {

        Progress.prompt(getProject(), executionInput, true,
                txt("prc.execution.title.LoadingDataDictionary"),
                txt("prc.execution.text.LoadingExecutableElements"),
                progress -> {
                    Project project = getProject();
                    MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                    MethodBrowserSettings settings = executionManager.getBrowserSettings();
                    DBMethod currentMethod = executionInput == null ? null : executionInput.getMethod();
                    if (currentMethod != null) {
                        currentMethod.getArguments();
                        settings.setSelectedConnection(currentMethod.getConnection());
                        settings.setSelectedSchema(currentMethod.getSchema());
                        settings.setSelectedMethod(currentMethod);
                    }

                    DBSchema schema = settings.getSelectedSchema();
                    ObjectTreeModel objectTreeModel = !debug || DatabaseFeature.DEBUGGING.isSupported(schema) ?
                            new ObjectTreeModel(schema, settings.getVisibleObjectTypes(), settings.getSelectedMethod()) :
                            new ObjectTreeModel(null, settings.getVisibleObjectTypes(), null);


                    Dialogs.show(() -> new MethodExecutionBrowserDialog(project, objectTreeModel, true), (dialog, exitCode) -> {
                        if (exitCode != DialogWrapper.OK_EXIT_CODE) return;

                        DBMethod method = dialog.getSelectedMethod();
                        MethodExecutionInput methodExecutionInput = executionManager.getExecutionInput(method);
                        if (callback != null && methodExecutionInput != null) {
                            callback.accept(methodExecutionInput);
                        }
                    });
                });
    }


    public void setExecutionInputs(List<MethodExecutionInput> executionInputs) {
        executionHistory.setExecutionInputs(executionInputs);
    }

    public void cleanupExecutionHistory(List<ConnectionId> connectionIds) {
        executionHistory.cleanupHistory(connectionIds);
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        @NonNls Element element = newStateElement();
        Element browserSettingsElement = newElement(element, "method-browser");
        browserSettings.writeConfiguration(browserSettingsElement);

        executionHistory.writeState(element);
        argumentValuesHistory.writeState(element);
        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        Element browserSettingsElement = element.getChild("method-browser");
        if (browserSettingsElement != null) {
            browserSettings.readConfiguration(browserSettingsElement);
        }

        executionHistory.readState(element);
        argumentValuesHistory.readState(element);
    }




    @Override
    public void disposeInner() {
        Disposer.dispose(executionHistory);
        super.disposeInner();
    }
}
