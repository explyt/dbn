package com.dbn.oracleAI;

import javax.swing.JPanel;

/**
 * Interface for Wizard step to implement.
 * A wizard step to be displayed on screen
 * should provide JComponent that compose its visual
 * representation (usually a Form)
 */
public interface WizardStepViewPortProvider {
  /**
   * Gets the panel to be displayed.
   * Method to be called by wizard when the implementor
   * step should appear on screen
   * @return the panel
   */
  JPanel getPanel();
}
