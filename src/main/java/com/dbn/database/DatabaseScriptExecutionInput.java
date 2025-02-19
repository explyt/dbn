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

package com.dbn.database;

import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.database.DatabaseInfo;
import com.dbn.connection.SchemaId;
import com.dbn.execution.script.CmdLineInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DatabaseScriptExecutionInput extends CmdLineExecutionInput{
    public DatabaseScriptExecutionInput(
            @NotNull CmdLineInterface cmdLineInterface,
            @NotNull String filePath,
            @NotNull String content,
            @Nullable SchemaId schemaId,
            @NotNull DatabaseInfo databaseInfo,
            @NotNull AuthenticationInfo authenticationInfo) {
        super(content);

        initExecutable(cmdLineInterface, databaseInfo, authenticationInfo);
        initAuthentication(authenticationInfo);
        initConsoleCommands(filePath, schemaId);
    }

    protected abstract void initExecutable(
            CmdLineInterface cmdLineInterface,
            DatabaseInfo databaseInfo,
            AuthenticationInfo authenticationInfo);

    protected abstract void initAuthentication(AuthenticationInfo authenticationInfo);

    protected abstract void initConsoleCommands(String filePath, SchemaId schemaId);
}
