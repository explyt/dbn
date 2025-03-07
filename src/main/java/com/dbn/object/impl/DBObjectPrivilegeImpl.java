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

package com.dbn.object.impl;

import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dbn.object.DBObjectPrivilege;
import com.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

class DBObjectPrivilegeImpl extends DBPrivilegeImpl<DBPrivilegeMetadata> implements DBObjectPrivilege {

    DBObjectPrivilegeImpl(ConnectionHandler connection, DBPrivilegeMetadata metadata) throws SQLException {
        super(connection, metadata);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.OBJECT_PRIVILEGE;
    }

}
