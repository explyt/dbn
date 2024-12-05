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

package com.dbn.connection.action;

import com.dbn.common.action.ContextAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.ConnectionRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public abstract class AbstractConnectionAction extends ContextAction<ConnectionHandler> {
    private final ConnectionRef connection;

    public AbstractConnectionAction(@NotNull ConnectionHandler connection) {
        this.connection = connection.ref();
    }

    public AbstractConnectionAction(String text, String description, Icon icon, @NotNull ConnectionHandler connection) {
        super(text, description, icon);
        this.connection = connection.ref();
    }

    public ConnectionId getConnectionId() {
        return connection.getConnectionId();
    }

    @Nullable
    public ConnectionHandler getConnection() {
        return ConnectionRef.get(connection);
    }

    @Override
    protected ConnectionHandler getContext(@NotNull AnActionEvent e) {
        return connection.get();
    }

    @NotNull
    @Override
    public Project getProject() {
        return connection.ensure().getProject();
    }
}

