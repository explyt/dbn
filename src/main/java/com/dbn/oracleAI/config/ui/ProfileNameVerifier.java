package com.dbn.oracleAI.config.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

import static com.dbn.nls.NlsResources.txt;

/**
 * InputVerifier class for AI profile name
 */
public class ProfileNameVerifier extends InputVerifier {
  private static final Border ERROR_BORDER = BorderFactory.createLineBorder(Color.RED, 1);
  private static final Border DEFAULT_BORDER = UIManager.getBorder("TextField.border");
  private final List<String> profileNames;
  private final boolean isUpdate;

  public ProfileNameVerifier(List<String> profileNames, boolean isUpdate) {
    this.profileNames = profileNames;
    this.isUpdate = isUpdate;
  }

  @Override
  public boolean verify(JComponent input) {
    JTextField textField = (JTextField) input;
    boolean isEmpty = textField.getText().trim().isEmpty();
    boolean exists = profileNames.contains(textField.getText().trim().toUpperCase());
    if (isEmpty) {
      textField.setBorder(ERROR_BORDER);
      textField.setToolTipText(txt("profile.mgmt.general_step.profile_name.validation.empty"));
    } else if (exists && !isUpdate) {
      textField.setBorder(ERROR_BORDER);
      textField.setToolTipText(txt("profile.mgmt.general_step.profile_name.validation.exists"));
    } else {
      textField.setBorder(DEFAULT_BORDER);
      textField.setToolTipText(null);
    }
    return !(isEmpty || exists);
  }

  @Override
  public boolean shouldYieldFocus(JComponent input) {
    return true;
  }
}
