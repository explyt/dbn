package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.Profile;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Profile edition Object list step for edition wizard
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionObjectListStep extends AbstractProfileEditionStep  {
  private JPanel profileEditionObjectListMainPane;
  private JComboBox comboBox1;
  private JCheckBox useAllCheckBox;
  private JTable table1;
  private JCheckBox checkBox1;
  private JTextField textField1;

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
