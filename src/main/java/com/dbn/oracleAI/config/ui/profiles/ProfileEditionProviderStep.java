package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.Profile;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 * Profile edition provider step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionProviderStep extends AbstractProfileEditionStep  {

  private JPanel profileEditionProviderMainPane;
  private JComboBox providerNameCombo;
  private JLabel providerNameLabel;
  private JLabel providerModelLabel;
  private JComboBox providerModelCombo;
  private JSlider temperatureSlider;

  public ProfileEditionProviderStep() {
    super();
  }
  public ProfileEditionProviderStep(Profile profile) {
    super();
  }

  @Override public JPanel getPanel() {
    return profileEditionProviderMainPane;
  }

  @Override public void setAttributesOn(Profile p) {
    // TODO : set others attrs
  }

}
