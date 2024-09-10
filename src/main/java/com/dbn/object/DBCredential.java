package com.dbn.object;

import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.type.DBCredentialType;

public interface DBCredential extends DBSchemaObject {
    DBCredentialType getType();

    String getUserName();

    String getComments();
}
