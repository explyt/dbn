package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.Profile;

import javax.swing.JPanel;

/**
 * Profile edition Object list step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionObjectListStep extends AbstractProfileEditionStep  {
  private JPanel profileEditionObjectListMainPane;

  public ProfileEditionObjectListStep() {
    super();
  }
  public ProfileEditionObjectListStep(Profile profile) {
    super();
  }

  @Override public JPanel getPanel() {
    return profileEditionObjectListMainPane;
  }

  @Override public void setAttributesOn(Profile p) {
    // TODO : set others attrs
  }
}
