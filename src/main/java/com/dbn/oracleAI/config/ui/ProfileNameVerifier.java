package com.dbn.oracleAI.config.ui;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Input verifier for an AI profile name
 */
public class ProfileNameVerifier extends InputVerifier implements
  ActionListener {

  @Override public void actionPerformed(ActionEvent e) {
    JTextField input = (JTextField)e.getSource();
    shouldYieldFocus(input); //ignore return value
  }
  @Override
  public boolean shouldYieldFocus(JComponent input) {
    //return verify(input);
    return true;
  }
  @Override public boolean verify(JComponent input) {
    // TODO : enough for now
    return ((JTextField)input).getText().length() > 0;
  }
}
