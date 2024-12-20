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

package com.dbn.database;

import com.dbn.common.property.Property;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.context.DatabaseContext;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

@Getter
public enum JdbcProperty implements Property.IntBase {
    MD_CATALOGS("Catalogs", true),
    MD_SCHEMAS("Schemas", true),
    MD_TABLES("Tables", true),
    MD_VIEWS("Views", true),
    MD_COLUMNS("Columns", true),
    MD_PSEUDO_COLUMNS("Pseudo columns", true),
    MD_INDEXES("Indexes", true),
    MD_PRIMARY_KEYS("Primary Keys", true),
    MD_IMPORTED_KEYS("Imported Keys", true),
    MD_FUNCTIONS("Functions", true),
    MD_FUNCTION_COLUMNS("Function columns", true),
    MD_PROCEDURES("Procedures", true),
    MD_PROCEDURE_COLUMNS("Procedure columns", true),
    SQL_DATASET_ALIASING("Dataset aliasing", true),

    CATALOG_AS_OWNER("Catalog as owner", false),
    ;

    public static final JdbcProperty[] VALUES = values();

    private final String description;
    private final boolean feature;
    private final IntMasks masks = new IntMasks(this);

    JdbcProperty(String description, boolean feature) {
        this.description = description;
        this.feature = feature;
    }

    @Override
    public IntMasks masks() {
        return masks;
    }

    public boolean isSupported(@Nullable ConnectionHandler connection) {
        return isValid(connection) && connection.getCompatibility().is(this);
    }

    public boolean isSupported(@Nullable DatabaseContext connectionProvider) {
        return isValid(connectionProvider) && isSupported(connectionProvider.getConnection());
    }
}
