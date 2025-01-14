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

package com.dbn.execution.java.ui;

import com.dbn.common.dispose.Disposed;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.options.setting.Settings;
import com.dbn.common.project.ProjectRef;
import com.dbn.common.state.PersistentStateElement;
import com.dbn.common.util.CollectionUtil;
import com.dbn.common.util.Unsafe;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ConnectionConfigListener;
import com.dbn.execution.java.JavaExecutionInput;
import com.dbn.object.*;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.dbn.common.dispose.Disposer.replace;
import static com.dbn.common.options.setting.Settings.newElement;

@Getter
@Setter
public class JavaExecutionHistory implements PersistentStateElement, ConnectionConfigListener, Disposable{
    private final ProjectRef project;
    private boolean groupEntries = true;
    private DBObjectRef<DBJavaMethod> selection;

    private List<JavaExecutionInput> executionInputs = CollectionUtil.createConcurrentList();

    public JavaExecutionHistory(Project project) {
        this.project = ProjectRef.of(project);
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public void setExecutionInputs(List<JavaExecutionInput> executionInputs) {
        this.executionInputs.clear();
        this.executionInputs.addAll(executionInputs);
    }

    public void cleanupHistory(List<ConnectionId> connectionIds) {
        executionInputs.removeIf(executionInput -> connectionIds.contains(executionInput.getConnectionId()));
    }

    public void connectionRemoved(ConnectionId connectionId) {
        executionInputs.removeIf(executionInput -> connectionId.equals(executionInput.getConnectionId()));
        if (selection != null && Objects.equals(selection.getConnectionId(), connectionId)) {
            selection = null;
        }
    }

    @Nullable
    public List<DBJavaMethod> getRecentlyExecutedMethods(@NotNull DBJavaClass program) {
        List<DBJavaMethod> recentObjects = new ArrayList<>();
        List<DBJavaMethod> methods = program.getMethods();
        for (DBJavaMethod method : methods) {
            JavaExecutionInput executionInput = getExecutionInput(method, false);
            if (executionInput != null) {
                recentObjects.add(method);
            }
        }
        return recentObjects.isEmpty() ? null : recentObjects;
    }

    @NotNull
    public JavaExecutionInput getExecutionInput(@NotNull DBJavaMethod method) {
        JavaExecutionInput executionInput = getExecutionInput(method, true);
        return Failsafe.nn(executionInput);
    }

    @Nullable
    public JavaExecutionInput getExecutionInput(@NotNull DBJavaMethod method, boolean create) {
        return getExecutionInput(method.ref(), create);
    }

    public JavaExecutionInput getExecutionInput(DBObjectRef<DBJavaMethod> method, boolean create) {
        for (JavaExecutionInput executionInput : executionInputs) {
            if (executionInput.getMethodRef().equals(method)) {
                return executionInput;
            }
        }

        if (create) {
            return createExecutionInput(method);
        }

        return null;
    }

    @NotNull
    private JavaExecutionInput createExecutionInput(@NotNull DBObjectRef<DBJavaMethod> method) {
        JavaExecutionInput executionInput = getExecutionInput(method, false);
        if (executionInput == null) {
            synchronized (this) {
                executionInput = getExecutionInput(method, false);
                if (executionInput == null) {
                    executionInput = new JavaExecutionInput(getProject(), method);
                    executionInputs.add(executionInput);
                    Collections.sort(executionInputs);
                    selection = method;
                    return executionInput;
                }
            }
        }
        return executionInput;
    }

    @Nullable
    public JavaExecutionInput getLastSelection() {
        if (selection != null) {
            for (JavaExecutionInput executionInput : executionInputs) {
                if (executionInput.getMethodRef().equals(selection)) {
                    return executionInput;
                }
            }
        }
        return null;
    }


    /*****************************************
     *         PersistentStateElement        *
     *****************************************/
    @Override
    public void readState(Element element) {
        executionInputs.clear();
        Element historyElement = element.getChild("execution-history");
        if (historyElement != null) {
            groupEntries = Settings.getBoolean(historyElement, "group-entries", groupEntries);

            Element executionInputsElement = historyElement.getChild("execution-inputs");
            for (Element child : executionInputsElement.getChildren()) {
                Unsafe.warned(() -> {
                    JavaExecutionInput executionInput = new JavaExecutionInput(getProject());
                    executionInput.readConfiguration(child);
                    if (getExecutionInput(executionInput.getMethodRef(), false) == null) {
                        executionInputs.add(executionInput);
                    }
                });
            }
            Collections.sort(executionInputs);

            Element selectionElement = historyElement.getChild("selection");
            if (selectionElement != null) {
                selection = new DBObjectRef<>();
                selection.readState(selectionElement);
            }
        }
    }

    @Override
    public void writeState(Element element) {
        Element historyElement = newElement(element, "execution-history");

        Settings.setBoolean(historyElement, "group-entries", groupEntries);

        Element configsElement = newElement(historyElement, "execution-inputs");
        for (JavaExecutionInput executionInput : this.executionInputs) {
            if (!executionInput.isObsolete()) {
                Element configElement = newElement(configsElement, "execution-input");
                executionInput.writeConfiguration(configElement);
            }
        }

        if (selection != null) {
            Element selectionElement = newElement(historyElement, "selection");
            selection.writeState(selectionElement);
        }
    }

    @Override
    public void dispose() {
        executionInputs = replace(executionInputs, Disposed.list());
    }


}
