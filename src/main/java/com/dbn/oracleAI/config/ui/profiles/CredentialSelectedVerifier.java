package com.dbn.oracleAI.config.ui.profiles;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

import static com.dbn.nls.NlsResources.txt;

/**
 * Input verifier for an AI profile credential
 */
public class CredentialSelectedVerifier extends InputVerifier {
  private static final Border ERROR_BORDER = BorderFactory.createLineBorder(Color.RED, 1);
  private static final Border DEFAULT_BORDER = UIManager.getBorder("TextField.border");


  @Override
  public boolean verify(JComponent input) {
    JComboBox comboBox = (JComboBox) input;
    boolean isValid = comboBox.getSelectedItem() != null;
    if (!isValid) {
      comboBox.setBorder(ERROR_BORDER);
      comboBox.setToolTipText(txt("profile.mgmt.general_step.credential_name.validation"));
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
