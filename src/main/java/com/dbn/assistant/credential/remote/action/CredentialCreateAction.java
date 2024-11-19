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

package com.dbn.assistant.credential.remote.action;

import com.dbn.assistant.credential.remote.ui.CredentialManagementForm;
import com.dbn.common.icon.Icons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Credential management creation action
 * (prompts an empty credential detail dialog)
 *
 * @author Dan Cioca (Oracle)
 */
public class CredentialCreateAction extends CredentialManagementAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        CredentialManagementForm managementForm = getManagementForm(e);
        if (managementForm == null) return;

        managementForm.promptCredentialCreation();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.ACTION_ADD);
        presentation.setText("Create Credential");
        presentation.setEnabled(isEnabled(e));
    }

    private static boolean isEnabled(@NotNull AnActionEvent e) {
        CredentialManagementForm managementForm = getManagementForm(e);
        if (managementForm == null) return false;
        if (managementForm.isLoading()) return false;

        return true;
    }
}