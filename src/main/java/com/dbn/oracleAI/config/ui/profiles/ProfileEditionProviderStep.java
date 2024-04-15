package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.WizardStepViewPortProvider;

import javax.swing.JPanel;

/**
 * Profile edition provider step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionProviderStep implements WizardStepViewPortProvider {

  private JPanel profileEditionProviderMainPane;

  @Override public JPanel getPanel() {
    return profileEditionProviderMainPane;
  }
}
