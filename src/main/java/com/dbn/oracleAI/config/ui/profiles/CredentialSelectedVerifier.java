package com.dbn.oracleAI.config.ui.profiles;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.util.Locale;
import java.util.ResourceBundle;

public class CredentialSelectedVerifier extends InputVerifier {
  private static final Border ERROR_BORDER = BorderFactory.createLineBorder(Color.RED, 1);
  private static final Border DEFAULT_BORDER = UIManager.getBorder("TextField.border");
  private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());


  @Override
  public boolean verify(JComponent input) {
    JComboBox comboBox = (JComboBox) input;
    boolean isValid = comboBox.getSelectedItem() != null;
    if (!isValid) {
      comboBox.setBorder(ERROR_BORDER);
      comboBox.setToolTipText(messages.getString("profile.mgmt.general_step.credential_name.validation"));
    } else {
      comboBox.setBorder(DEFAULT_BORDER);
      comboBox.setToolTipText(null);
    }
    return isValid;
  }

  @Override
  public boolean shouldYieldFocus(JComponent input) {
    return true;
  }


}
