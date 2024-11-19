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

package com.dbn.assistant.profile.action;

import com.dbn.assistant.profile.ui.ProfileManagementForm;
import com.dbn.common.action.DataKeys;
import com.dbn.common.action.ProjectAction;
import com.dbn.object.DBAIProfile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Generic stub for actions related to management of profiles
 * (features the lookup of the profile management form from the context)
 *
 * @author Dan Cioca (Oracle)
 */
public abstract class ProfileManagementAction extends ProjectAction {
    @Nullable
    protected static ProfileManagementForm getManagementForm(@NotNull AnActionEvent e) {
        return e.getData(DataKeys.PROFILE_MANAGEMENT_FORM);
    }

    @Nullable
    protected DBAIProfile getSelectedProfile(@NotNull AnActionEvent e) {
        ProfileManagementForm managementForm = getManagementForm(e);
        return managementForm == null ? null : managementForm.getSelectedProfile();
    }
}