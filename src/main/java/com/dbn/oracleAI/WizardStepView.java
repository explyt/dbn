package com.dbn.oracleAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * View on a wizard.
 * Wizard is composed of steps. To walk through
 * these steps, i.e go back and forth a view is instanciated
 * upon wizard model. A model contain more step that a view expose
 * @param <E> type of list of step that view will wrap.
 */
public class WizardStepView<E> {
  //List of steps fo that view
  private final List<E> backend;
  //current position in the list .i.e current step
  private int currentStepIdx;
  private List<ViewEventListener> listeners = new ArrayList<>();

  /**
   * Creates a new view for the given step list.
   * @param data list of steps
   */
  public WizardStepView(List<E> data) {
    this.backend = Collections.unmodifiableList(data);
    this.currentStepIdx = 0;
  }

  /**
   * Gets the current step.
   * @return the current stop in the wizard flow
   */
  public E current() {
    return this.backend.get(this.currentStepIdx);
  }

  /**
   * Checks that this view can go backward.
   * A view usually go by one next/previous step. When the view
   * is positioned on the first step, it cannot go backward anymore
   * @return true of the view can still shift left (go backward). false otherwise
   */
  public boolean canBackward() {
    return (this.currentStepIdx > 0);
  }

  /**
   * Go backward, Shift this view one previous sep.
   *
   * @throws IllegalStateException if the view si already on the first step. Caller can check this by calling canBackward method
   */
  public void backward() throws IllegalStateException {
    if (this.currentStepIdx == 0) {
      throw new IllegalStateException("cannot move backward");
    }
    this.currentStepIdx--;
    this.listeners.forEach(listener -> {
      listener.notifyViewChange();
    });
  }

  /**
   * Checks that this view can go forward.
   * A view usually go by one next/previous step. When the view
   * is positioned on the last step, it cannot go forward anymore
   * @return true of the view can still shift right (go forward). false otherwise
   */
  public boolean canForward() {
    return (this.currentStepIdx < this.backend.size() - 1);
  }

  /**
   * Go forward, Shift this view one next sep.
   *
   * @throws IllegalStateException if the view si already on the last step. Caller can check this by calling canForward method
   */
  public void forward() throws IllegalStateException  {
    if (this.currentStepIdx == this.backend.size() - 1 ) {
      throw new IllegalStateException("cannot move forwad");
    }
    this.currentStepIdx++;
    this.listeners.forEach(listener -> {
      listener.notifyViewChange();
    });
  }

  /**
   * Gets the current wizard view progression indicator.
   * This progression is based on current step and total number of steps we have in this view.
   * @return a percentage as an int between 0 and 100.
   */
  public int progress() {
    return (int) Math.abs((((float)this.currentStepIdx) / ((float)this.backend.size())) * 100.0);
  }

  @Override public String toString() {
    return "WizardStepView{" + "size=" + this.backend.size() + ", idx=" + currentStepIdx
           + ", current=" + this.backend.get(this.currentStepIdx) + '}';
  }

  /**
   * Adds a listener to this view change.
   * @param listener a ViewEventListener listener that is fired when the view goes back or forth.
   */
  public void addListener(ViewEventListener listener) {
    this.listeners.add(listener);
  }
}
