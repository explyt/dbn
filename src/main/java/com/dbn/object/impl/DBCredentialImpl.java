package com.dbn.object.impl;

import com.dbn.browser.ui.HtmlToolTipBuilder;
import com.dbn.connection.ConnectionHandler;
import com.dbn.database.common.metadata.def.DBCredentialMetadata;
import com.dbn.object.DBCredential;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBSchemaObjectImpl;
import com.dbn.object.common.operation.DBOperationExecutor;
import com.dbn.object.common.status.DBObjectStatus;
import com.dbn.object.type.DBCredentialType;
import com.dbn.object.type.DBObjectType;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.credentials.CredentialManagementService;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dbn.object.common.property.DBObjectProperty.DISABLEABLE;
import static com.dbn.object.common.property.DBObjectProperty.SCHEMA_OBJECT;

@Getter
class DBCredentialImpl extends DBSchemaObjectImpl<DBCredentialMetadata> implements DBCredential {
    private DBCredentialType type;
    private String userName;
    private String comments;

    DBCredentialImpl(DBSchema parent, DBCredentialMetadata resultSet) throws SQLException {
        super(parent, resultSet);
    }

    @Override
    protected String initObject(ConnectionHandler connection, DBObject parentObject, DBCredentialMetadata metadata) throws SQLException {
        String name = metadata.getCredentialName();
        type = DBCredentialType.valueOf(metadata.getCredentialType());
        userName = metadata.getUserName();
        comments= metadata.getComments();

        return name;
    }

    @Override
    protected void initProperties() {
        properties.set(SCHEMA_OBJECT, true);
        properties.set(DISABLEABLE, true);
    }

    @Override
    public void initStatus(DBCredentialMetadata metadata) throws SQLException {
        boolean enabled = metadata.isEnabled();
        getStatus().set(DBObjectStatus.ENABLED, enabled);
    }


    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.CREDENTIAL;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    @Override
    public DBOperationExecutor getOperationExecutor() {
        return operationType -> {
            CredentialManagementService managementService = CredentialManagementService.getInstance(getProject());
            ConnectionHandler connection = getConnection();
            Credential aiCredential = toAICredential();
            switch (operationType) {
                case ENABLE:  managementService.enableCredential(connection, aiCredential, null); break;
                case DISABLE: managementService.disableCredential(connection, aiCredential, null); break;
            }
        };
    }

    @Deprecated // decommission ai specific Credential type
    private Credential toAICredential() {
        return new Credential(getName(), DBCredentialType.PASSWORD, getUserName(), isEnabled(), getComments());
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/

    @Override
    public boolean isLeaf() {
        return true;
    }

}
