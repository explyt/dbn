/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI;

import com.dbn.common.component.ConnectionComponent;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.interfaces.DatabaseAssistantInterface;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * Component stub for database assistant services
 * Exposes utilities from {@link ConnectionComponent} as well as connectivity and interface access
 *
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public abstract class AIAssistantComponent extends ConnectionComponent {
    public AIAssistantComponent(@NotNull ConnectionHandler connection) {
        super(connection);
    }

    protected final DatabaseAssistantInterface getAssistantInterface() {
        return getConnection().getAssistantInterface();
    }

    protected final DBNConnection getAssistantConnection() throws SQLException {
        return getConnection(SessionId.ORACLE_AI);
    }
}
