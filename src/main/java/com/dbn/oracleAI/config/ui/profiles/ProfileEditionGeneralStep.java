package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.WizardStepViewPortProvider;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ProfileEditionGeneralStep implements WizardStepViewPortProvider {
  private JPanel profileEditionGeneralMainPane;
  private JTextField nameTextField;
  private JComboBox credentialComboBox;
  private JTextField descriptionTextField;

  public JPanel getPanel() {
    return profileEditionGeneralMainPane;
  }
}
