package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.WizardStepEventListener;
import com.dbn.oracleAI.WizardStepViewPortProvider;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProfileEditionStep implements
  WizardStepViewPortProvider {

  protected List<WizardStepEventListener> listeners = new ArrayList<>();
  @Override public void addEventListener(WizardStepEventListener listener) {
    listeners.add(listener);
  }

  @Override public boolean isInputsValid() {
    return true;
  }

  @Override public boolean isInputsChanged() {
    return false;
  }
}
