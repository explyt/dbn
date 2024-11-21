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

package com.dbn.connection.mapping;

import com.dbn.common.state.PersistentStateElement;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SchemaId;
import com.dbn.connection.SessionId;
import com.dbn.connection.context.DatabaseContextBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FileConnectionContext extends DatabaseContextBase, PersistentStateElement {
    String getFileUrl();

    @Nullable
    VirtualFile getFile();

    default boolean isForFile(@NotNull VirtualFile file) {
        return file.equals(getFile());
    }

    default void setFileUrl(String fileUrl) {
        throw new UnsupportedOperationException();
    }

    default boolean setConnectionId(@Nullable ConnectionId connectionId) {
        throw new UnsupportedOperationException();
    }

    default boolean setSessionId(@Nullable SessionId sessionId) {
        throw new UnsupportedOperationException();
    }

    default boolean setSchemaId(@Nullable SchemaId schemaId) {
        throw new UnsupportedOperationException();
    }

}
