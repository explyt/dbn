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

package com.dbn.object.management.adapter.credential;

import com.dbn.common.util.Strings;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.interfaces.DatabaseAssistantInterface;
import com.dbn.object.DBCredential;
import com.dbn.object.management.adapter.shared.DBObjectUpdateAdapter;
import com.dbn.object.type.DBAttributeType;

import java.sql.SQLException;
import java.util.Map;

/**
 * Implementation of the {@link com.dbn.object.management.ObjectManagementAdapter} specialized in updating entities of type {@link DBCredential}
 * @author Dan Cioca (Oracle)
 */
public class DBCredentialUpdateAdapter extends DBObjectUpdateAdapter<DBCredential> {

    public DBCredentialUpdateAdapter(DBCredential credential) {
        super(credential);
    }

    @Override
    protected void invokeDatabaseInterface(ConnectionHandler connection, DBNConnection conn, DBCredential credential) throws SQLException {
        DatabaseAssistantInterface assistantInterface = connection.getAssistantInterface();
        Map<DBAttributeType, String> attributes = credential.getAttributes();
        String credentialName = credential.getName();

        // update attributes
        for (DBAttributeType attribute : attributes.keySet()) {
            String value = attributes.get(attribute);
            if (Strings.isEmpty(value)) continue;
            assistantInterface.updateCredentialAttribute(conn, credentialName, attribute.getId(), value);
        }

        // update status
        if (credential.isEnabled())
            assistantInterface.enableCredential(conn, credentialName); else
            assistantInterface.disableCredential(conn, credentialName);
    }
}
