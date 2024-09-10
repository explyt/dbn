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

import com.dbn.common.exception.Exceptions;
import com.dbn.common.outcome.OutcomeHandler;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.util.TextFields;
import com.dbn.common.util.Dialogs;
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.OciCredential;
import com.dbn.oracleAI.config.PasswordCredential;
import com.dbn.oracleAI.config.credentials.CredentialManagementService;
import com.dbn.oracleAI.config.providers.AIProviderCredential;
import com.dbn.oracleAI.config.providers.AIProviderCredentialSettings;
import com.dbn.oracleAI.config.providers.AIProviderSettings;
import com.dbn.oracleAI.service.AICredentialService;
import com.dbn.oracleAI.service.AICredentialServiceImpl;
import com.dbn.oracleAI.types.CredentialType;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;
import java.util.Set;

import static com.dbn.common.ui.CardLayouts.showCard;

/**
 * A dialog window for creating new AI credentials.
 * This window allows users to input credential information, supporting different types of credentials.
 * It interacts with {@link AICredentialServiceImpl} to create credentials in the system.
 */
@Getter
public class CredentialEditForm extends DBNFormBase {

  private final AICredentialService credentialSvc;
  private JPanel mainPanel;
  private JTextField credentialNameField;
  private JComboBox<CredentialType> credentialTypeComboBox;
  private JTextField passwordCredentialUsernameField;
  private javax.swing.JPasswordField passwordCredentialPasswordField;
  private JPanel attributesPane;
  private JTextField ociCredentialUserOcidField;
  private JTextField ociCredentialUserTenancyOcidField;
  private JTextField ociCredentialPrivateKeyField;
  private JTextField ociCredentialFingerprintField;
  private JButton localCredentialPickerButton;
  private JCheckBox saveLocalCheckBox;
  private JCheckBox statusCheckBox;
  private JPanel passwordCard;
  private JPanel ociCard;
  private JLabel errorLabel;


  private final ConnectionRef connection;
  private Credential credential;
  private AIProviderCredential localCredential;
  private final Set<String> usedCredentialNames;

  /**
   * Constructs a CredentialEditForm
   *
   * @param dialog the parent dialog
   * @param credential the credential to be edited, can be null in case of credential creation
   * @param usedCredentialNames the names of credentials which are already defined and name can no longer be used
   */
  public CredentialEditForm(CredentialEditDialog dialog, @Nullable Credential credential, Set<String> usedCredentialNames) {
    super(dialog);
    this.connection = dialog.getConnection().ref();
    this.credentialSvc = AICredentialService.getInstance(getConnection());
    this.credential = credential;
    this.usedCredentialNames = usedCredentialNames;

    initCredentialTypeComboBox();
    initCredentialPickerButton();
    initCredentialAttributeFields();
  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }

  private ConnectionHandler getConnection() {
    return connection.ensure();
  }

  private void initCredentialTypeComboBox() {
    credentialTypeComboBox.addItem(CredentialType.PASSWORD);
    credentialTypeComboBox.addItem(CredentialType.OCI);
    credentialTypeComboBox.addActionListener((e) -> showCard(attributesPane, credentialTypeComboBox.getSelectedItem()));
    credentialTypeComboBox.setEnabled(credential == null);
  }

  private void initCredentialPickerButton() {
    localCredentialPickerButton.addActionListener((e) -> showCredentialPicker());
  }

  private void showCredentialPicker() {
    Dialogs.show(() -> new CredentialPickerDialog(getProject(), c -> pickLocalCredential(c)));
  }

  private void pickLocalCredential(AIProviderCredential credential) {
    localCredential = credential;
    passwordCredentialUsernameField.setText(credential.getUser());
    passwordCredentialPasswordField.setText(credential.getKey());
    saveLocalCheckBox.setEnabled(false);
  }

  /**
   * Populate fields with the attributes of the credential to be updated
   */
  private void initCredentialAttributeFields() {
    if (credential == null) return;

    credentialNameField.setText(credential.getName());
    credentialNameField.setEnabled(false);
    statusCheckBox.setSelected(credential.isEnabled());
    if (credential instanceof PasswordCredential) {
      initPasswordCredentialFields();
    } else if (credential instanceof OciCredential) {
      initOciCredentialFields();
    }
  }

  private void initOciCredentialFields() {
    credentialTypeComboBox.setSelectedItem(CredentialType.OCI);
    OciCredential ociCredentialProvider = (OciCredential) credential;
    ociCredentialUserOcidField.setText(ociCredentialProvider.getUserName());
    ociCredentialUserTenancyOcidField.setText(ociCredentialProvider.getUserTenancyOCID());
    ociCredentialPrivateKeyField.setText(ociCredentialProvider.getPrivateKey());
    ociCredentialFingerprintField.setText(ociCredentialProvider.getFingerprint());
  }

  private void initPasswordCredentialFields() {
    credentialTypeComboBox.setSelectedItem(CredentialType.PASSWORD);
    passwordCredentialUsernameField.setText(credential.getUserName());
    TextFields.onTextChange(passwordCredentialUsernameField, e -> initLocalSaveCheckbox());
    TextFields.onTextChange(passwordCredentialPasswordField, e -> initLocalSaveCheckbox());
  }

  private void initLocalSaveCheckbox() {
    boolean enabled = canSaveLocalCredential();
    boolean selected = enabled && saveLocalCheckBox.isSelected();
    saveLocalCheckBox.setEnabled(enabled);
    saveLocalCheckBox.setSelected(selected);
  }

  private boolean canSaveLocalCredential() {
    if (localCredential == null) return true; // local credential has been chosen to fill the attributes
    return
        !Objects.equals(localCredential.getUser(), passwordCredentialUsernameField.getText()) ||
        !Objects.equals(localCredential.getKey(), passwordCredentialPasswordField.getText());
  }


  /**
   * Collects the fields' info and sends them to the service layer to create new credential
   */
  protected void doCreateAction(OutcomeHandler successHandler) {
    credential = createCredential();
    if (credential == null) return;
    getManagementService().createCredential(getConnection(), credential, successHandler);

/*
    credentialSvc.create(credential)
            .thenAccept(e -> credentialSvc.updateStatus(credential.getName(), statusCheckBox.isSelected()))
            .exceptionally(this::handleException);
*/
  }

  /**
   * Collects the fields' info and sends them to the service layer to update new credential
   */
  protected void doUpdateAction(OutcomeHandler successHandler) {
    credential = createCredential();
    if (credential == null) return;
    getManagementService().updateCredential(getConnection(), credential, successHandler);

/*
    boolean enabled = statusCheckBox.isSelected();
    boolean statusChanged = credential.isEnabled() != enabled;
    credentialSvc.update(editedCredential)
        .thenAccept(e -> when(statusChanged, () -> credentialSvc.updateStatus(credential.getCredentialName(), enabled)))
        .exceptionally(this::handleException);
*/
  }

  @NotNull
  private CredentialManagementService getManagementService() {
    return CredentialManagementService.getInstance(ensureProject());
  }

  private Void handleException(Throwable e) {
    Dispatch.run(mainPanel, () -> Messages.showErrorDialog(getProject(), Exceptions.causeMessage(e)));
    return null;
  }

  @Nullable
  private Credential createCredential() {
    CredentialType credentialType = (CredentialType) credentialTypeComboBox.getSelectedItem();
    if (credentialType == null) return null;

    Credential credential =
            credentialType == CredentialType.PASSWORD ? createPwdCredential() :
            credentialType == CredentialType.OCI ? createOciCredential() :
            null;

    credential.setEnabled(statusCheckBox.isSelected());
    return credential;
  }

  private PasswordCredential createPwdCredential() {
    return new PasswordCredential(
            credentialNameField.getText(),
            passwordCredentialUsernameField.getText(),
            passwordCredentialPasswordField.getText());
  }

  private OciCredential createOciCredential() {
    return new OciCredential(
            credentialNameField.getText(),
            ociCredentialUserOcidField.getText(),
            ociCredentialUserTenancyOcidField.getText(),
            ociCredentialPrivateKeyField.getText(),
            ociCredentialFingerprintField.getText());
  }

  protected void saveProviderInfo() {
    Project project = ensureProject();
    AIProviderCredentialSettings settings = AIProviderSettings.getInstance(project).getCredentialSettings();
    AIProviderCredential aiProviderCredential = new AIProviderCredential();
    aiProviderCredential.setName(credentialNameField.getText());
    aiProviderCredential.setUser(passwordCredentialUsernameField.getText());
    aiProviderCredential.setKey(passwordCredentialPasswordField.getText());
    settings.getCredentials().add(aiProviderCredential);
  }
}
