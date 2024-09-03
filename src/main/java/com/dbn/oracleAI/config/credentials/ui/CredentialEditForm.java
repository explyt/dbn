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
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.AIProviders.AIProviderCredential;
import com.dbn.oracleAI.config.AIProviders.AIProviderCredentialGeneralSettings;
import com.dbn.oracleAI.config.AIProviders.AIProvidersSettings;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.OciCredential;
import com.dbn.oracleAI.config.PasswordCredential;
import com.dbn.oracleAI.config.ui.ProvidersSelectionCallback;
import com.dbn.oracleAI.service.AICredentialService;
import com.dbn.oracleAI.service.AICredentialServiceImpl;
import com.dbn.oracleAI.types.CredentialType;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

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
  private JButton keyProviderPickerButton;
  private JCheckBox saveInfoCheckBox;
  private JCheckBox statusCheckBox;
  private JPanel passwordCard;
  private JPanel ociCard;
  private JLabel errorLabel;


  private Credential credential;
  private final Set<String> usedCredentialNames;

  /**
   * Constructs a CredentialEditForm
   *
   * @param connection the connection for which the credential is being created / updated
   * @param credential the credential to be edited, can be null in case of credential creation
   * @param usedCredentialNames the names of credentials which are already defined and name can no longer be used
   */
  public CredentialEditForm(ConnectionHandler connection, @Nullable Credential credential, Set<String> usedCredentialNames) {
    super(connection);
    this.credentialSvc = AICredentialService.getInstance(connection);
    this.credential = credential;
    this.usedCredentialNames = usedCredentialNames;
    initializeUI();
  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }

  /**
   * Initializes the user interface components and event listeners for the dialog.
   */
  private void initializeUI() {
    saveInfoCheckBox.setText(txt("ai.settings.credentials.info.save"));
    if (credential != null) {
      hydrateFields();
    } else {
      credentialTypeComboBox.addItem(CredentialType.PASSWORD);
      credentialTypeComboBox.addItem(CredentialType.OCI);
      credentialTypeComboBox.addActionListener((e) -> {
        if (credentialTypeComboBox.getSelectedItem() == CredentialType.PASSWORD) saveInfoCheckBox.setVisible(true);
        else saveInfoCheckBox.setVisible(false);
        CardLayout cl = (CardLayout) (attributesPane.getLayout());
        cl.show(attributesPane, credentialTypeComboBox.getSelectedItem().toString());
      });
    }
    keyProviderPickerButton.addActionListener((e) -> {
      ProvidersSelectionCallback providersSelectionCallback = aiProviderType -> populateFields(aiProviderType.getUsername(), aiProviderType.getKey());
      AiProviderSelection aiProviderSelection = new AiProviderSelection(getProject(), providersSelectionCallback);
      aiProviderSelection.showAndGet();

    });
  }

  private void populateFields(String username, String key) {
    credentialTypeComboBox.setSelectedItem(CredentialType.PASSWORD);
    passwordCredentialUsernameField.setText(username);
    passwordCredentialPasswordField.setText(key);
  }

  /**
   * Populate fields with the attributes of the credential to be updated
   */
  private void hydrateFields() {
    credentialNameField.setText(credential.getCredentialName());
    credentialNameField.setEnabled(false);
    statusCheckBox.setSelected(credential.isEnabled());
    if (credential instanceof PasswordCredential) {
      credentialTypeComboBox.addItem(CredentialType.PASSWORD);
      passwordCredentialUsernameField.setText(credential.getUsername());
    } else if (credential instanceof OciCredential) {
      credentialTypeComboBox.addItem(CredentialType.OCI);
      OciCredential ociCredentialProvider = (OciCredential) credential;
      ociCredentialUserOcidField.setText(ociCredentialProvider.getUsername());
      ociCredentialUserTenancyOcidField.setText(ociCredentialProvider.getUserTenancyOCID());
      ociCredentialPrivateKeyField.setText(ociCredentialProvider.getPrivateKey());
      ociCredentialFingerprintField.setText(ociCredentialProvider.getFingerprint());
    }
    credentialTypeComboBox.addItem(CredentialType.PASSWORD);
    credentialTypeComboBox.setEnabled(false);
  }


  /**
   * Collects the fields' info and sends them to the service layer to create new credential
   */
  protected void doCreateAction() {
    CredentialType credentialType = (CredentialType) credentialTypeComboBox.getSelectedItem();
    credential = null;
    switch (credentialType) {
      case PASSWORD:
        credential = new PasswordCredential(credentialNameField.getText(), passwordCredentialUsernameField.getText(), passwordCredentialPasswordField.getText());
        break;
      case OCI:
        credential = new OciCredential(credentialNameField.getText(), ociCredentialUserOcidField.getText(),
            ociCredentialUserTenancyOcidField.getText(), ociCredentialPrivateKeyField.getText(), ociCredentialFingerprintField.getText());
    }
    boolean isEnabled = statusCheckBox.isSelected();
    credentialSvc.createCredential(credential)
        .thenAccept(e -> {
          if (!isEnabled) credentialSvc.updateStatus(credential.getCredentialName(), statusCheckBox.isSelected());
        })
        .exceptionally(this::handleException);

  }

  private Void handleException(Throwable e) {
    SwingUtilities.invokeLater(() -> Messages.showErrorDialog(getProject(), e.getCause().getMessage()));
    return null;
  }

  /**
   * Collects the fields' info and sends them to the service layer to update new credential
   */
  protected void doUpdateAction() {
    CredentialType credentialType = CredentialType.valueOf(credentialTypeComboBox.getSelectedItem().toString());
    Credential editedCredential = null;
    switch (credentialType) {
      case PASSWORD:
        editedCredential = new PasswordCredential(credentialNameField.getText(), passwordCredentialUsernameField.getText(), passwordCredentialPasswordField.getText());
        break;
      case OCI:
        editedCredential = new OciCredential(credentialNameField.getText(), ociCredentialUserOcidField.getText(),
            ociCredentialUserTenancyOcidField.getText(), ociCredentialPrivateKeyField.getText(), ociCredentialFingerprintField.getText());
    }
    boolean isEnabled = credential.isEnabled() != statusCheckBox.isSelected();
    credentialSvc.updateCredential(editedCredential)
        .thenAccept(e -> {
          if (isEnabled) credentialSvc.updateStatus(credential.getCredentialName(), statusCheckBox.isSelected());
        })
        .exceptionally(this::handleException);
  }


  protected void saveProviderInfo() {
    Project project = ensureProject();
    AIProviderCredentialGeneralSettings settings = AIProvidersSettings.getInstance(project).getGeneralSettings();
    AIProviderCredential aiProviderCredential = new AIProviderCredential();
    aiProviderCredential.setCredentialName(credentialNameField.getText());
    aiProviderCredential.setUsername(passwordCredentialUsernameField.getText());
    aiProviderCredential.setKey(passwordCredentialPasswordField.getText());
    settings.getAIProviderTypes().add(aiProviderCredential);
  }
}
