package com.dbn.oracleAI;

import javax.swing.JPanel;

/**
 * Wizard step interface.
 */
public interface WizardStep {
  /**
   * Gets the title of this step.
   * @return the title of the step to be displayed
   */
  public String getTitle();

  /**
   * Gets the UI panel for the step.
   *
   * @return the step panel
   */
  public JPanel getViewPort();

}
