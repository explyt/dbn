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

package com.dbn.oracleAI.config.credentials.ui;

import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.util.Borders;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.service.AIProfileService;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class CredentialDetailsForm extends DBNFormBase {
    private JPanel mainPanel;
    private JTextField credentialNameTextField;
    private JTextField userNameTextField;
    private JTextField commentsTextField;
    private JCheckBox enabledCheckBox;
    private JList<String> usageList;
    private JScrollPane usageScrollPane;

    private final Credential credential;

    public CredentialDetailsForm(@NotNull CredentialManagementForm parent, Credential credential) {
        super(parent);
        this.credential = credential;

        initCredentialFields();
        initUsageList();
    }

    private void initUsageList() {
        ConnectionHandler connection = getManagementForm().getConnection();
        AIProfileService profileService = AIProfileService.getInstance(connection);
        profileService.list().thenAccept(profiles -> updateUsageList(profiles));
    }

    private void updateUsageList(List<Profile> profiles) {
        String credentialName = credential.getCredentialName();
        List<String> usedByProfiles =  getManagementForm().getCredentialUsage(credentialName);
        usageList.setListData(usedByProfiles.toArray(new String[0]));
        usageList.setBorder(Borders.EMPTY_BORDER);
        usageList.setCellRenderer(createListCellRenderer());
    }

    private static @NotNull ColoredListCellRenderer<String> createListCellRenderer() {
        return new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends String> list, String value, int index, boolean selected, boolean hasFocus) {
                append(value, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        };
    }

    public CredentialManagementForm getManagementForm() {
        return super.getParentComponent();
    }

    private void initCredentialFields() {
        credentialNameTextField.setText(credential.getCredentialName());
        userNameTextField.setText(credential.getUsername());
        commentsTextField.setText(credential.getComments());
        enabledCheckBox.setSelected(credential.isEnabled());
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
