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

package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ui.ProfileNameVerifier;
import com.dbn.oracleAI.service.AICredentialService;
import com.intellij.icons.AllIcons;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.util.Set;

import static com.dbn.nls.NlsResources.txt;

/**
 * Profile edition general step for edition wizard.
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionGeneralStep extends WizardStep<ProfileEditionWizardModel> {
  private JPanel profileEditionGeneralMainPane;
  private JTextField nameTextField;
  private JComboBox<String> credentialComboBox;
  private JTextField descriptionTextField;
  private final AICredentialService credentialSvc;

  private final Profile profile;
  private final Set<String> existingProfileNames;

  private final boolean isUpdate;

  public ProfileEditionGeneralStep(ConnectionHandler connection, Profile profile, Set<String> existingProfileNames, boolean isUpdate) {
    super(txt("profile.mgmt.general_step.title"),
            txt("profile.mgmt.general_step.explaination"),
            AllIcons.General.Settings);
    this.profile = profile;
    this.existingProfileNames = existingProfileNames;
    this.isUpdate = isUpdate;

    this.credentialSvc = AICredentialService.getInstance(connection);

    initializeUI();
    addValidationListener();
    if (!isUpdate) populateCredentials();
  }

  private void initializeUI() {
    if (isUpdate) {
      nameTextField.setText(profile.getProfileName());
      descriptionTextField.setText(profile.getDescription());
      credentialComboBox.addItem(profile.getCredentialName());
      nameTextField.setEnabled(false);
      credentialComboBox.setEnabled(true);
      descriptionTextField.setEnabled(false);
    }
  }

  private void addValidationListener() {
    nameTextField.setInputVerifier(new ProfileNameVerifier(existingProfileNames, isUpdate));
    credentialComboBox.setInputVerifier(new CredentialSelectedVerifier());
    nameTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }

      public void removeUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }

      public void insertUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }
    });
    credentialComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        InputVerifier verifier = credentialComboBox.getInputVerifier();
        if (verifier != null) {
          verifier.verify(credentialComboBox);
        }
      }
    });

  }

  private void populateCredentials() {
    credentialSvc.list().thenAccept(credentialProviderList -> {
      SwingUtilities.invokeLater(() -> {
        credentialComboBox.removeAllItems();
        for (Credential credential : credentialProviderList) {
          credentialComboBox.addItem(credential.getName());
        }
        if (!credentialProviderList.isEmpty()) {
          credentialComboBox.setSelectedIndex(0);
        }
      });
    });
  }

  @Override
  public JComponent prepare(WizardNavigationState wizardNavigationState) {
    return profileEditionGeneralMainPane;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return nameTextField;
  }

  @Override
  public WizardStep<ProfileEditionWizardModel> onNext(ProfileEditionWizardModel model) {
    nameTextField.getInputVerifier().verify(nameTextField);
    credentialComboBox.getInputVerifier().verify(credentialComboBox);
    profile.setProfileName(nameTextField.getText());
    profile.setCredentialName((String) credentialComboBox.getSelectedItem());
    // special case for description: null and empty string is the same
    //    do not confuse Profile.equals() because of that
    if (descriptionTextField.getText().isEmpty()) {
      // did the user really remove the description or was it missing
      // from the beginning ?
      if (profile.getDescription() != null && !profile.getDescription().isEmpty()) {
        profile.setDescription(descriptionTextField.getText());
      }
    } else {
      // set it in any case
      profile.setDescription(descriptionTextField.getText());
    }



    return super.onNext(model);
  }

}
