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

package com.dbn.connection.security;

import com.dbn.connection.ConnectionComponentBase;
import com.dbn.connection.ConnectionHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The DatabaseSecurityMonitor class provides functionality to monitor and manage
 * quoted identifiers within a database context. It maintains a thread-safe map
 * of identifiers to their respective quoted versions. This is useful for ensuring
 * consistent and secure handling of database identifiers by applying a quoting mechanism.
 *
 * @author Dan Cioca (Oracle)
 */
public class DatabaseSecurityMonitor extends ConnectionComponentBase {
    private final Map<String, String> quotedIdentifiers = new ConcurrentHashMap<>();

    public DatabaseSecurityMonitor(ConnectionHandler connection) {
        super(connection);
    }

    public String getQuotedIdentifier(String identifier) {
        return quotedIdentifiers.get(identifier);
    }

    public void recordIdentifier(String identifier, Function<String, String> identifierQuoter) {
        quotedIdentifiers.computeIfAbsent(identifier, identifierQuoter);
    }

    @Override
    public void disposeInner() {
        quotedIdentifiers.clear();
    }
}
