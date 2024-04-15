package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.WizardStepViewPortProvider;
import com.dbn.oracleAI.config.ui.ProfileNameVerifier;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Profile edition general step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionGeneralStep implements WizardStepViewPortProvider {
  private JPanel profileEditionGeneralMainPane;
  private JTextField nameTextField;
  private JComboBox credentialComboBox;
  private JTextField descriptionTextField;

  private boolean isFormValid() {
    // TODO : add more 
    return nameTextField.getInputVerifier().verify(nameTextField);
  }

  public ProfileEditionGeneralStep() {
    nameTextField.setInputVerifier(new ProfileNameVerifier());
  }

  public JPanel getPanel() {
    return profileEditionGeneralMainPane;
  }
}
