package com.dbn.oracleAI.config.ui.profiles;

import javax.swing.JPanel;

/**
 * Profile edition Object list step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionObjectListStep extends AbstractProfileEditionStep  {
  private JPanel profileEditionObjectListMainPane;

  @Override public JPanel getPanel() {
    return profileEditionObjectListMainPane;
  }

}
