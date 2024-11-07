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

package com.dbn.object.management.adapter.constraint;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.object.DBConstraint;
import com.dbn.object.management.adapter.shared.DBObjectCreateAdapter;

import java.sql.SQLException;

/**
 * Implementation of the {@link com.dbn.object.management.ObjectManagementAdapter} specialized in creating entities of type {@link DBConstraint}
 *
 * @author Dan Cioca (Oracle)
 */
public class DBConstraintCreateAdapter extends DBObjectCreateAdapter<DBConstraint> {

    public DBConstraintCreateAdapter(DBConstraint object) {
        super(object);
    }

    @Override
    protected void invokeDatabaseInterface(ConnectionHandler connection, DBNConnection conn, DBConstraint object) throws SQLException {
        throw new UnsupportedOperationException();
    }
}
