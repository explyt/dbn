/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI.config.credentials.action;

import com.dbn.common.action.DataKeys;
import com.dbn.common.action.ToggleAction;
import com.dbn.common.icon.Icons;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.credentials.CredentialManagementService;
import com.dbn.oracleAI.config.credentials.ui.CredentialManagementForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Toggle action for the credential management dialogs, allowing to quickly enable or disable a Credential
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class CredentialStatusToggleAction extends ToggleAction {
    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        Credential credential = getSelectedCredential(e);
        return credential != null && credential.isEnabled();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean selected) {
        CredentialManagementForm managementForm = getManagementForm(e);
        if (managementForm == null) return;

        Credential credential = managementForm.getSelectedCredential();
        if (credential == null) return;

        ConnectionHandler connection = managementForm.getConnection();
        Project project = connection.getProject();
        CredentialManagementService managementService = CredentialManagementService.getInstance(project);

        if (selected)
            managementService.enableCredential(connection, credential, null); else
            managementService.disableCredential(connection, credential, null);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(
                isSelected(e) ?
                        Icons.COMMON_FILTER_ACTIVE :
                        Icons.COMMON_FILTER_INACTIVE);

        presentation.setEnabled(isEnabled(e));
    }

    private static @Nullable CredentialManagementForm getManagementForm(@NotNull AnActionEvent e) {
        return e.getData(DataKeys.CREDENTIAL_MANAGEMENT_FORM);
    }

    private static Credential getSelectedCredential(@NotNull AnActionEvent e) {
        CredentialManagementForm managementForm = getManagementForm(e);
        if (managementForm == null) return null;

        return managementForm.getSelectedCredential();
    }

    private static boolean isEnabled(@NotNull AnActionEvent e) {
        CredentialManagementForm managementForm = getManagementForm(e);
        if (managementForm == null) return false;
        if (managementForm.isLoading()) return false;
        if (managementForm.getSelectedCredential() == null) return false;

        return true;
    }
}
