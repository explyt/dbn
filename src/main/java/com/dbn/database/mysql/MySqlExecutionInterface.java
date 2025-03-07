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

package com.dbn.database.mysql;

import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.database.DatabaseInfo;
import com.dbn.connection.SchemaId;
import com.dbn.database.CmdLineExecutionInput;
import com.dbn.database.common.DatabaseExecutionInterfaceImpl;
import com.dbn.database.common.execution.JavaExecutionProcessor;
import com.dbn.database.common.execution.MethodExecutionProcessor;
import com.dbn.execution.script.CmdLineInterface;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MySqlExecutionInterface extends DatabaseExecutionInterfaceImpl {

    @Override
    public MethodExecutionProcessor createExecutionProcessor(DBMethod method) {
        return createSimpleMethodExecutionProcessor(method);
    }

    @Override
    public MethodExecutionProcessor createDebugExecutionProcessor(DBMethod method) {
        return null;
    }

    @Override
    public JavaExecutionProcessor createExecutionProcessor(DBJavaMethod method) {return null;}

    @Override
    public CmdLineExecutionInput createScriptExecutionInput(
            @NotNull CmdLineInterface cmdLineInterface,
            @NotNull String filePath,
            @NotNull String content,
            @Nullable SchemaId schemaId,
            @NotNull DatabaseInfo databaseInfo,
            @NotNull AuthenticationInfo authenticationInfo) {

        return new MySqlScriptExecutionInput(
                cmdLineInterface,
                filePath,
                content,
                schemaId,
                databaseInfo,
                authenticationInfo);
    }
}