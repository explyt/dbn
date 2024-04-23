package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.WizardStepChangeEvent;
import com.dbn.oracleAI.WizardStepEventListener;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ui.ProfileNameVerifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Profile edition general step for edition wizard
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionGeneralStep extends AbstractProfileEditionStep {
  private JPanel profileEditionGeneralMainPane;
  private JTextField nameTextField;
  private JComboBox credentialComboBox;
  private JTextField descriptionTextField;
  private final AICredentialService credentialSvc;


  public ProfileEditionGeneralStep(Project project, @Nullable Profile profile) {
    super();
    if (profile == null) {
      this.credentialSvc = project.getService(DatabaseOracleAIManager.class).getCredentialService();
      nameTextField.setInputVerifier(new ProfileNameVerifier());
      nameTextField.addActionListener(e -> {
        for (WizardStepEventListener listener : this.listeners) {
          listener.onStepChange(new WizardStepChangeEvent(this));
        }
      });
      populateCredentials();
    } else {
      this.credentialSvc = project.getService(DatabaseOracleAIManager.class).getCredentialService();
      // TODO : do we authorize name to be edited
      nameTextField.setText(profile.getProfileName());
      descriptionTextField.setText(profile.getDescription());
      credentialComboBox.addItem(profile.getCredentialName());
      credentialComboBox.setSelectedIndex(0);
      credentialComboBox.setEnabled(false);
    }

  }

  private void populateCredentials() {
    ApplicationManager.getApplication().invokeLater(() -> {
      credentialSvc.listCredentials().thenAccept(credentialProviderList -> {

        for (Credential credential : credentialProviderList) {
          credentialComboBox.addItem(credential.getCredentialName());
          credentialComboBox.setSelectedIndex(0);
        }
      });
    });
  }

  @Override
  public JPanel getPanel() {
    return profileEditionGeneralMainPane;
  }

  @Override
  public boolean isInputsValid() {
    // TODO : add more
    return nameTextField.getInputVerifier().verify(nameTextField);
  }

  @Override
  public void setAttributesOn(Profile p) {
    p.setProfileName(nameTextField.getText());
    p.setCredentialName(credentialComboBox.getSelectedItem().toString());
    p.setDescription(descriptionTextField.getText());
  }

}
