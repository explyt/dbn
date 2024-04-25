package com.dbn.oracleAI.config.ui;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Input verifier for an AI profile name
 */
public class SelectedObjectItemsVerifier extends InputVerifier implements
    ActionListener {

  @Override
  public void actionPerformed(ActionEvent e) {
    JTable input = (JTable) e.getSource();
    shouldYieldFocus(input); //ignore return value
  }

  @Override
  public boolean shouldYieldFocus(JComponent input) {
    //return verify(input);
    return true;
  }

  @Override
  public boolean verify(JComponent input) {
    // TODO : enough for now
    return ((JTable) input).getRowCount() > 0;
  }
}

