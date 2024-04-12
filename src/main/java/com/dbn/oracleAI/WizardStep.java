package com.dbn.oracleAI;

import javax.swing.JPanel;

public interface WizardStep {
  public String getTitle();
  public JPanel getViewPort();

}
