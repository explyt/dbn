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

package com.dbn.execution.method.browser.action;

import com.dbn.common.action.BasicAction;
import com.dbn.connection.ConnectionHandler;
import com.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ConnectionSelectAction extends BasicAction {
    private final ConnectionHandler connection;
    private MethodExecutionBrowserForm browserComponent;

    ConnectionSelectAction(MethodExecutionBrowserForm browserComponent, ConnectionHandler connection) {
        super(connection.getName(), null, connection.getIcon());
        this.browserComponent = browserComponent;
        this.connection = connection;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        browserComponent.setConnectionHandler(connection);
    }


}
