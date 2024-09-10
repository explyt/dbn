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

package com.dbn.oracleAI.config.credentials.adapter;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.interfaces.DatabaseAssistantInterface;
import com.dbn.object.event.ObjectChangeAction;
import com.dbn.object.management.ObjectManagementAdapterBase;
import com.dbn.object.type.DBObjectType;
import com.dbn.oracleAI.config.Credential;
import org.jetbrains.annotations.Nls;

import java.sql.SQLException;

/**
 * Implementation of the {@link com.dbn.object.management.ObjectManagementAdapter} specialized in enabling entities of type {@link Credential}
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class CredentialEnableAdapter extends ObjectManagementAdapterBase<Credential> {

    public CredentialEnableAdapter(ConnectionHandler connection) {
        super(connection, DBObjectType.CREDENTIAL, ObjectChangeAction.ENABLE);
    }

    @Nls
    @Override
    protected String getProcessTitle() {
        return txt("prc.assistant.title.EnablingCredential");
    }

    @Nls
    @Override
    protected String getProcessDescription(Credential entity) {
        return txt("prc.assistant.message.EnablingCredential", entity.getType(), entity.getName());
    }

    @Nls
    @Override
    protected String getSuccessMessage(Credential entity) {
        return txt("msg.assistant.info.CredentialEnablingSuccess", entity.getType(), entity.getName());
    }

    @Nls
    @Override
    protected String getFailureMessage(Credential entity) {
        return txt("msg.assistant.error.CredentialEnablingFailure", entity.getType(), entity.getName());
    }

    @Override
    protected void invokeDatabaseInterface(ConnectionHandler connection, DBNConnection conn, Credential credential) throws SQLException {
        DatabaseAssistantInterface assistantInterface = connection.getAssistantInterface();
        assistantInterface.enableCredential(conn, credential.getName());
    }
}
