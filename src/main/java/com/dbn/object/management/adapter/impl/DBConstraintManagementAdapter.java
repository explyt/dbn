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

package com.dbn.object.management.adapter.impl;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.interfaces.DatabaseDataDefinitionInterface;
import com.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dbn.object.DBConstraint;
import com.dbn.object.management.ObjectManagementAdapterFactory;
import com.dbn.object.management.ObjectManagementAdapterFactoryBase;

import java.sql.SQLException;

/**
 * Implementation of {@link ObjectManagementAdapterFactory} for objects of type {@link com.dbn.object.DBConstraint}
 * @author Dan Cioca (Oracle)
 */
public class DBConstraintManagementAdapter extends ObjectManagementAdapterFactoryBase<DBConstraint> {

    @Override
    protected void createObject(ConnectionHandler connection, DBNConnection conn, DBConstraint object) throws SQLException {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    protected void updateObject(ConnectionHandler connection, DBNConnection conn, DBConstraint object) throws SQLException {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    protected void deleteObject(ConnectionHandler connection, DBNConnection conn, DBConstraint object) throws SQLException {
        DatabaseDataDefinitionInterface databaseInterface = connection.getDataDefinitionInterface();
        databaseInterface.dropObject(
                object.getTypeName(),
                object.getQualifiedName(), conn);
    }

    @Override
    protected void enableObject(ConnectionHandler connection, DBNConnection conn, DBConstraint object) throws SQLException {
        DatabaseMetadataInterface databaseInterface = connection.getMetadataInterface();;
        databaseInterface.enableConstraint(
                object.getSchemaName(),
                object.getDataset().getName(),
                object.getName(),
                conn);
    }

    @Override
    protected void disableObject(ConnectionHandler connection, DBNConnection conn, DBConstraint object) throws SQLException {
        DatabaseMetadataInterface databaseInterface = connection.getMetadataInterface();;
        databaseInterface.disableConstraint(
                object.getSchemaName(),
                object.getDataset().getName(),
                object.getName(),
                conn);
    }
}
