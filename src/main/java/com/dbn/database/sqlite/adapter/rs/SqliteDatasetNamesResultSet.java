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

package com.dbn.database.sqlite.adapter.rs;

import com.dbn.common.cache.CacheKey;
import com.dbn.database.sqlite.adapter.SqliteMetadataResultSet;
import com.dbn.database.sqlite.adapter.SqliteMetadataResultSetRow;
import com.dbn.database.sqlite.adapter.SqliteRawMetaData.TableNames;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * DATASET_NAME
 */

public abstract class SqliteDatasetNamesResultSet extends SqliteMetadataResultSet<SqliteDatasetNamesResultSet.Dataset> {
    protected String ownerName;
    protected SqliteDatasetNamesResultSet(String ownerName) throws SQLException {
        this.ownerName = ownerName;
        TableNames tableNames = getTableNames();

        for (TableNames.Row row : tableNames.getRows()) {
            Dataset element = new Dataset();
            element.datasetName = row.getName();
            add(element);
        }
    }

    private TableNames getTableNames() throws SQLException {
        return cache().get(
                CacheKey.key(ownerName, "DATASET_NAMES"),
                () -> new TableNames(loadTableNames()));
    }

    protected abstract ResultSet loadTableNames() throws SQLException;

    @Override
    public String getString(String columnLabel) throws SQLException {
        Dataset element = current();
        if (Objects.equals(columnLabel, "DATASET_NAME")) {
            return element.datasetName;
        }
        return null;
    }

    static class Dataset implements SqliteMetadataResultSetRow<Dataset> {
        private String datasetName;

        @Override
        public String identifier() {
            return datasetName;
        }
    }
}
