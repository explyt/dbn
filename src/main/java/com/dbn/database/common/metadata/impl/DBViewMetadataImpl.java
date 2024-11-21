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

package com.dbn.database.common.metadata.impl;

import com.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dbn.database.common.metadata.def.DBViewMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBViewMetadataImpl extends DBObjectMetadataBase implements DBViewMetadata {

    public DBViewMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getViewName() throws SQLException {
        return getString("VIEW_NAME");
    }

    public String getViewType() throws SQLException {
        return getString("VIEW_TYPE");
    }

    public String getViewTypeOwner() throws SQLException {
        return getString("VIEW_TYPE_OWNER");
    }

    public boolean isSystemView() throws SQLException {
        return isYesFlag("IS_SYSTEM_VIEW");
    }
}
