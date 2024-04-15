package com.dbn.oracleAI;

import lombok.Getter;

/**
 * Event class for WizardStepEventListeners
 */
public class WizardStepChangeEvent {
  public WizardStepChangeEvent(WizardStepViewPortProvider provider) {

    //TODO: may be excessif to pass te entire step.
    //      refine later
     this.provider = provider;
  }

  @Getter private WizardStepViewPortProvider provider;

}
