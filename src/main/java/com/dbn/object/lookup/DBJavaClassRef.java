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

import com.dbn.common.util.Java;
import com.dbn.connection.ConnectionId;
import com.dbn.object.DBJavaClass;
import com.dbn.object.DBSchema;
import com.dbn.object.type.DBJavaValueType;
import com.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public final class DBJavaClassRef {

    private final DBObjectRef<DBJavaClass>[] refs;

    public DBJavaClassRef(DBSchema schema, String className, String ... alternativeSchemas) {
        DBObjectType objectType = Java.isPrimitive(className) ?
                DBObjectType.JAVA_PRIMITIVE :
                DBObjectType.JAVA_CLASS;

        refs = new DBObjectRef[alternativeSchemas.length + 1];
        refs[0] = new DBObjectRef<>(schema.ref(), objectType, className);

        ConnectionId connectionId = schema.getConnectionId();
        for (int i = 0; i < alternativeSchemas.length; i++) {
            String schemaName = alternativeSchemas[i];
            if (schemaName.equalsIgnoreCase(schema.getName())) continue;
            DBObjectRef<DBSchema> sysSchema = new DBObjectRef<>(connectionId, DBObjectType.SCHEMA, schemaName);
            refs[i + 1] = new DBObjectRef<>(sysSchema, objectType, className);
        }
    }

    public String getObjectName() {
        return refs[0].getObjectName();
    }

    public String getSimpleName() {
        return DBJavaNameCache.getSimpleName(getObjectName());
    }

    public String getCanonicalName() {
        return DBJavaNameCache.getCanonicalName(getObjectName());
    }

    @Nullable
    public DBJavaClass get() {
        for (DBObjectRef<DBJavaClass> ref : refs) {
            DBJavaClass javaClass = ref.get();
            if (javaClass != null) return javaClass;
        }

        return null;
    }

    public boolean isLoaded() {
        return Arrays.stream(refs).anyMatch(ref -> ref.isLoaded());
    }

    public boolean isPrimitive() {
        return Java.isPrimitive(getObjectName());
    }

    /**
     * Determines whether the current object represents a pseudo-primitive type.
     * A pseudo-primitive type is a value type that is either a standard Java primitive,
     * its corresponding wrapper class, or commonly used value types such as {@code String},
     * {@code Number}, {@code BigDecimal}, or atomic value types.
     *
     * @return {@code true} if the object corresponds to a pseudo-primitive type;
     *         {@code false} otherwise.
     */
    public boolean isPseudoPrimitive() {
        return DBJavaValueType.forObjectName(getObjectName()) != null;
    }

    public boolean isVoid() {
        return Objects.equals(getObjectName(), "void");
    }

    @Override
    public boolean equals(Object o) {
        // compare first lookup level only
        if (o == null || getClass() != o.getClass()) return false;
        DBJavaClassRef that = (DBJavaClassRef) o;
        return Objects.equals(refs[0], that.refs[0]);
    }

    @Override
    public int hashCode() {
        // use first lookup level only
        return Objects.hashCode(refs[0]);
    }
}
