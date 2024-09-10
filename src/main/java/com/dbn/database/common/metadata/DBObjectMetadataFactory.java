package com.dbn.database.common.metadata;

import com.dbn.common.content.DynamicContentType;
import com.dbn.database.common.metadata.impl.*;
import com.dbn.object.type.DBObjectRelationType;
import com.dbn.object.type.DBObjectType;

import java.sql.ResultSet;

public class DBObjectMetadataFactory {
    public static final DBObjectMetadataFactory INSTANCE = new DBObjectMetadataFactory();

    private DBObjectMetadataFactory() {}

    public <M extends DBObjectMetadata> M create(DynamicContentType contentType, ResultSet resultSet) {
        M metadata = null;
        if (contentType instanceof DBObjectType) {
            DBObjectType objectType = (DBObjectType) contentType;
            metadata = (M) createMetadata(objectType, resultSet);

        } else if (contentType instanceof DBObjectRelationType) {
            DBObjectRelationType relationType = (DBObjectRelationType) contentType;
            metadata = (M) createMetadata(relationType, resultSet);
        }


        return metadata;
    }

    private DBObjectMetadata createMetadata(DBObjectType objectType, ResultSet resultSet) {
        switch (objectType) {
            case USER:                return new DBUserMetadataImpl(resultSet);
            case ROLE:                return new DBRoleMetadataImpl(resultSet);
            case PRIVILEGE:           return new DBPrivilegeMetadataImpl(resultSet);
            case SCHEMA:              return new DBSchemaMetadataImpl(resultSet);
            case DBLINK:              return new DBDatabaseLinkMetadataImpl(resultSet);
            case CHARSET:             return new DBCharsetMetadataImpl(resultSet);
            case CLUSTER:             return new DBClusterMetadataImpl(resultSet);
            case CREDENTIAL:          return new DBCredentialMetadataImpl(resultSet);
            case OBJECT_PRIVILEGE:    return new DBPrivilegeMetadataImpl(resultSet);
            case SYSTEM_PRIVILEGE:    return new DBPrivilegeMetadataImpl(resultSet);
            case PROCEDURE:           return new DBProcedureMetadataImpl(resultSet);
            case FUNCTION:            return new DBFunctionMetadataImpl(resultSet);
            case TYPE:                return new DBTypeMetadataImpl(resultSet);
            case TYPE_FUNCTION:       return new DBFunctionMetadataImpl(resultSet);
            case TYPE_PROCEDURE:      return new DBProcedureMetadataImpl(resultSet);
            case TYPE_ATTRIBUTE:      return new DBTypeAttributeMetadataImpl(resultSet);
            case PACKAGE:             return new DBPackageMetadataImpl(resultSet);
            case PACKAGE_TYPE:        return new DBTypeMetadataImpl(resultSet);
            case PACKAGE_FUNCTION:    return new DBFunctionMetadataImpl(resultSet);
            case PACKAGE_PROCEDURE:   return new DBProcedureMetadataImpl(resultSet);
            case DIMENSION:           return new DBDimensionMetadataImpl(resultSet);
            case VIEW:                return new DBViewMetadataImpl(resultSet);
            case TABLE:               return new DBTableMetadataImpl(resultSet);
            case NESTED_TABLE:        return new DBNestedTableMetadataImpl(resultSet);
            case MATERIALIZED_VIEW:   return new DBMaterializedViewMetadataImpl(resultSet);
            case SYNONYM:             return new DBSynonymMetadataImpl(resultSet);
            case SEQUENCE:            return new DBSequenceMetadataImpl(resultSet);
            case INDEX:               return new DBIndexMetadataImpl(resultSet);
            case COLUMN:              return new DBColumnMetadataImpl(resultSet);
            case CONSTRAINT:          return new DBConstraintMetadataImpl(resultSet);
            case ARGUMENT:            return new DBArgumentMetadataImpl(resultSet);
            case DATABASE_TRIGGER:    return new DBTriggerMetadataImpl(resultSet);
            case DATASET_TRIGGER:     return new DBTriggerMetadataImpl(resultSet);
            case INCOMING_DEPENDENCY: return new DBObjectDependencyMetadataImpl(resultSet);
            case OUTGOING_DEPENDENCY: return new DBObjectDependencyMetadataImpl(resultSet);
        }
        throw new UnsupportedOperationException("No provider defined for " + objectType);
    }

    private DBObjectMetadata createMetadata(DBObjectRelationType relationType, ResultSet resultSet) {
        switch (relationType) {
            case INDEX_COLUMN:      return new DBIndexColumnMetadataImpl(resultSet);
            case CONSTRAINT_COLUMN: return new DBConstraintColumnMetadataImpl(resultSet);
            case USER_ROLE:         return new DBGrantedRoleMetadataImpl(resultSet);
            case USER_PRIVILEGE:    return new DBGrantedPrivilegeMetadataImpl(resultSet);
            case ROLE_ROLE:         return new DBGrantedRoleMetadataImpl(resultSet);
            case ROLE_PRIVILEGE:    return new DBGrantedPrivilegeMetadataImpl(resultSet);
        }
        throw new UnsupportedOperationException("No provider defined for " + relationType);
    }


}
