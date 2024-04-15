package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.WizardStepViewPortProvider;

import javax.swing.JPanel;

/**
 * Profile edition Object list step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionObjectListStep implements WizardStepViewPortProvider {
  private JPanel profileEditionObjectListMainPane;

  @Override public JPanel getPanel() {
    return profileEditionObjectListMainPane;
  }
}
