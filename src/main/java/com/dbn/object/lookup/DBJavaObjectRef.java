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

package com.dbn.object.lookup;

import com.dbn.connection.ConnectionId;
import com.dbn.object.DBJavaObject;
import com.dbn.object.DBSchema;
import com.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBJavaObjectRef{
    private final List<DBObjectRef<DBJavaObject>> lookups = new ArrayList<>();

    public DBJavaObjectRef(DBSchema schema, String className, String ... alternativeSchemas) {
        lookups.add(new DBObjectRef<>(schema.ref(), DBObjectType.JAVA_OBJECT, className));

        ConnectionId connectionId = schema.getConnectionId();
        for (String schemaName : alternativeSchemas) {
            if (schemaName.equalsIgnoreCase(schema.getName())) continue;
            DBObjectRef<DBSchema> sysSchema = new DBObjectRef<>(connectionId, DBObjectType.SCHEMA, schemaName);
            lookups.add(new DBObjectRef<>(sysSchema, DBObjectType.JAVA_OBJECT, className));
        }
    }

    public String getClassName() {
        return lookups.get(0).getObjectName();
    }

    public String getClassSimpleName() {
        return getClassName().substring(getClassName().lastIndexOf("/") + 1);
    }

    @Nullable
    public DBJavaObject get() {
        for (DBObjectRef<DBJavaObject> lookup : lookups) {
            DBJavaObject javaObject = lookup.get();
            if (javaObject != null) return javaObject;
        }

        return null;
    }
}
