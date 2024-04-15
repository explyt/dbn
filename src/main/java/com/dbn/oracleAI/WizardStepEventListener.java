package com.dbn.oracleAI;

import java.util.EventListener;

/**
 * Wizard step event listener
 */
public interface WizardStepEventListener extends EventListener {
  /**
   * Implemented by listeners to receive event when a change happen
   * on a wizard step.
   */
  public void onStepChange(WizardStepChangeEvent event);
}
