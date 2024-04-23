package com.dbn.oracleAI;

import com.dbn.oracleAI.config.Profile;

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
   *
   * @return the panel
   */
  JPanel getPanel();

  /**
   * Checks that data model of this provider are correct
   *
   * @return true if vliad , false otherwise
   */
  boolean isInputsValid();

  /**
   * Checks that data model of this provider have changed
   *
   * @return true if some input data has changed , false otherwise
   */
  boolean isInputsChanged();

  /**
   * Adds a listener to this step provider
   *
   * @param listener the listener
   */
  void addEventListener(WizardStepEventListener listener);

  void setAttributesOn(Profile p);
}
