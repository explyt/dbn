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

package com.dbn.debugger.common.process;

import com.dbn.common.property.PropertyHolder;
import com.dbn.common.ui.Presentable;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.interfaces.DatabaseDebuggerInterface;
import com.dbn.debugger.DBDebugConsoleLogger;
import com.dbn.execution.ExecutionTarget;
import com.intellij.openapi.project.Project;

public interface DBDebugProcess extends Presentable, PropertyHolder<DBDebugProcessStatus> {
    ConnectionHandler getConnection();

    DBDebugConsoleLogger getConsole();

    Project getProject();

    DatabaseDebuggerInterface getDebuggerInterface();

    ExecutionTarget getExecutionTarget();
}
