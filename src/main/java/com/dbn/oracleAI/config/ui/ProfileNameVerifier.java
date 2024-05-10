package com.dbn.oracleAI.config.ui;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;

public class ProfileNameVerifier extends InputVerifier {
  private static final Border ERROR_BORDER = BorderFactory.createLineBorder(Color.RED, 1);
  private static final Border DEFAULT_BORDER = UIManager.getBorder("TextField.border");

  @Override
  public boolean verify(JComponent input) {
    JTextField textField = (JTextField) input;
    boolean isValid = !textField.getText().trim().isEmpty();
    if (!isValid) {
      textField.setBorder(ERROR_BORDER);
      textField.setToolTipText("This field cannot be empty.");
    } else {
      textField.setBorder(DEFAULT_BORDER);
      textField.setToolTipText(null);
    }
    return isValid;
  }

  @Override
  public boolean shouldYieldFocus(JComponent input) {
    return true;
  }
}
