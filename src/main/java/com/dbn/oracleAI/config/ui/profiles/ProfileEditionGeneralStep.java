package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.WizardStepChangeEvent;
import com.dbn.oracleAI.WizardStepEventListener;
import com.dbn.oracleAI.config.ui.ProfileNameVerifier;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Profile edition general step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionGeneralStep extends AbstractProfileEditionStep {
  private JPanel profileEditionGeneralMainPane;
  private JTextField nameTextField;
  private JComboBox credentialComboBox;
  private JTextField descriptionTextField;

  public ProfileEditionGeneralStep() {

    nameTextField.setInputVerifier(new ProfileNameVerifier());
    nameTextField.addActionListener(e->{
      for (WizardStepEventListener listener : this.listeners) {
        listener.onStepChange(new WizardStepChangeEvent(this));
      }
    });
    //((JTextField)input).setBorder(Borders.lineBorder(Color.RED));

  }

  public JPanel getPanel() {
    return profileEditionGeneralMainPane;
  }

  @Override public boolean isInputsValid() {
    // TODO : add more
    return nameTextField.getInputVerifier().verify(nameTextField);
  }


}
