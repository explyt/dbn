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

package com.dbn.database.common.security;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.connection.jdbc.DBNResource;
import com.dbn.connection.jdbc.DBNResultSet;
import com.dbn.connection.jdbc.DBNStatement;
import com.dbn.connection.security.DatabaseSecurityMonitor;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static com.dbn.common.dispose.Failsafe.nd;

/**
 * The {@code ObjectIdentifierMonitor} class acts as an {@link InvocationHandler} for dynamic proxy objects
 * to monitor and manage method calls that return specific object identifiers, particularly
 * those decorated with the {@code @ObjectIdentifier} annotation. It integrates with database security
 * mechanisms to handle and record quoted identifiers for secure usage.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Intercepts method calls on a proxied object.</li>
 *   <li>Monitors return values of methods annotated with {@code @ObjectIdentifier}.</li>
 *   <li>Integrates with a {@link DatabaseSecurityMonitor} to record and manage quoted identifiers.</li>
 * </ul>
 * <p>
 * Dependencies:
 * <ul>
 *   <li>Relies on a {@link DBNResource} instance to initialize database-related operations.</li>
 *   <li>Requires {@link DatabaseSecurityMonitor} and {@link DBNStatement} for identifier quoting and record management.</li>
 * </ul>
 *
 * @param <T> The type of the proxied target object.
 * @author Dan Cioca (Oracle)
 */
public class ObjectIdentifierMonitor<T> implements InvocationHandler {
    private final T target;
    private final DBNResource resource;
    private final DatabaseSecurityMonitor securityMonitor;
    private final DBNStatement statement;

    public ObjectIdentifierMonitor(T target, DBNResource resource) {
        this.target = target;
        this.resource = resource;

        this.securityMonitor = initSecurityMonitor();
        this.statement = initStatement();
    }

    private DatabaseSecurityMonitor initSecurityMonitor() {
        DBNConnection conn = nd(resource.getConnection());
        ConnectionHandler connection = nd(conn.getConnectionHandler());
        return connection.getSecurityMonitor();
    }

    @SneakyThrows
    private DBNStatement initStatement() {
        if (resource instanceof DBNStatement) {
            return (DBNStatement) resource;
        }

        if (resource instanceof DBNResultSet) {
            DBNResultSet resultSet = (DBNResultSet) resource;
            return nd(resultSet.getStatement());
        }

        DBNConnection connection = nd(resource.getConnection());
        return connection.createStatement();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(target, args);
        if (!isIdentifier(result)) return result;

        ObjectIdentifier annotation = method.getAnnotation(ObjectIdentifier.class);
        if (annotation == null) return result;

        String identifier = (String) result;
        securityMonitor.recordIdentifier(identifier, i -> quoteIdentifier(identifier));

        return result;
    }

    private boolean isIdentifier(Object result) {
        return result instanceof String;
    }

    @SneakyThrows
    private String quoteIdentifier(String identifier) {
        return statement.enquoteIdentifier(identifier, true);
    }

}
