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

package com.dbn.connection;

import com.dbn.common.project.Projects;
import com.dbn.common.util.TimeUtil;
import com.intellij.openapi.project.Project;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class ConnectionPoolCleaner extends TimerTask {
    public static final ConnectionPoolCleaner INSTANCE = new ConnectionPoolCleaner();


    @Override
    public void run() {
        for (Project project : Projects.getOpenProjects()) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(project);
            List<ConnectionHandler> connections = connectionManager.getConnections();
            for (ConnectionHandler connection : connections) {
                ConnectionPool connectionPool = connection.getConnectionPool();
                connectionPool.clean();
            }

        }
    }

    void start() {
        Timer poolCleaner = new Timer("DBN - Idle Connection Pool Cleaner");
        poolCleaner.schedule(INSTANCE, TimeUtil.Millis.ONE_MINUTE, TimeUtil.Millis.ONE_MINUTE);
    }

}


