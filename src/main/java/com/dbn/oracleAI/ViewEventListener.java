package com.dbn.oracleAI;

import java.util.EventListener;

/**
 * Eventing interface for wizard view change.
 */
public interface ViewEventListener extends EventListener {
  /**
   * this method is called whenever the view move to one step forward or backward.
   */
  void notifyViewChange();
}
