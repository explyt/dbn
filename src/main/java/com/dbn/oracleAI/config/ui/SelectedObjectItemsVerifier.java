package com.dbn.oracleAI.config.ui;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Input verifier for an AI profile name
 */
public class SelectedObjectItemsVerifier extends InputVerifier implements
    ActionListener {


  private static final Border ERROR_BORDER = BorderFactory.createLineBorder(Color.RED, 1);
  private static final Border DEFAULT_BORDER = UIManager.getBorder("TextField.border");
  private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());


  @Override
  public void actionPerformed(ActionEvent e) {
    JTable input = (JTable) e.getSource();
    shouldYieldFocus(input);
  }

  @Override
  public boolean shouldYieldFocus(JComponent input) {
    //return verify(input);
    return true;
  }

  @Override
  public boolean verify(JComponent input) {
    JTable profileTable = (JTable) input;
    boolean isValid = profileTable.getRowCount() > 0;
    if (!isValid) {
      profileTable.setBorder(ERROR_BORDER);
      profileTable.setToolTipText(messages.getString("profile.mgmt.object_list_step.validation"));
    } else {
      profileTable.setBorder(DEFAULT_BORDER);
      profileTable.setToolTipText(null);
    }
    return isValid;
  }
}

