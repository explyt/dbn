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

  /**
   * Check that this step is valid. That is
   * basically all inputs have expected value;
   * @return true of valid, false otherwsie
   */
  boolean isValid();

  /**
   * Adds a listener to this step change.
   * @param listener a listener that is fired when the step changed.
   */
  public void addListener(WizardStepEventListener listener);

}
