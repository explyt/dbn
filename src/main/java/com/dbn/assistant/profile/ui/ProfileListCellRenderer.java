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

package com.dbn.assistant.profile.ui;

import com.dbn.assistant.DatabaseAssistantManager;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.nls.NlsSupport;
import com.dbn.object.DBAIProfile;
import com.dbn.object.common.ui.DBObjectListCellRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.JList;

import static com.intellij.ui.SimpleTextAttributes.GRAY_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.REGULAR_ATTRIBUTES;

public class ProfileListCellRenderer extends DBObjectListCellRenderer<DBAIProfile> implements NlsSupport {
    private final ConnectionRef connection;

    public ProfileListCellRenderer(ConnectionHandler connection) {
        this.connection = ConnectionRef.of(connection);
    }

    private ConnectionHandler getConnection() {
        return ConnectionRef.ensure(connection);
    }

    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends DBAIProfile> list, DBAIProfile profile, int index, boolean selected, boolean hasFocus) {
        super.customizeCellRenderer(list, profile, index, selected, hasFocus);

        boolean enabled = list.isEnabled() && profile.isEnabled();
        SimpleTextAttributes attributes = enabled ? REGULAR_ATTRIBUTES : GRAY_ATTRIBUTES;
        if (isDefault(profile)) append(" (default)", attributes);

        setToolTipText(enabled ? null : txt("cfg.assistant.tooltip.ProfileDisabled"));
    }

    private boolean isDefault(DBAIProfile profile) {
        if (profile == null) return false;

        Project project = getConnection().getProject();
        DatabaseAssistantManager assistantManager = DatabaseAssistantManager.getInstance(project);
        return assistantManager.isDefaultProfile(connection.getConnectionId(), profile);
    }
}
