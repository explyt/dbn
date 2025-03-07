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

package com.dbn.common.properties.ui;

import com.dbn.common.ui.form.DBNForm;
import com.dbn.common.ui.table.DBNEditableTable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.dbn.common.ui.util.Accessibility.setAccessibleName;

public class PropertiesEditorTable extends DBNEditableTable<PropertiesTableModel> {

    public PropertiesEditorTable(@NotNull DBNForm parent, Map<String, String> properties) {
        super(parent, new PropertiesTableModel(properties), true);
        setAccessibleName(this, "Properties");
    }

    public void setProperties(Map<String, String> properties) {
        setModel(new PropertiesTableModel(properties));
    }
}