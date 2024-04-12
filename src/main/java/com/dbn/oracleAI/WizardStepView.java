package com.dbn.oracleAI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WizardStepView<E> {
  private final List<E> backend;
  private int idx;
  private List<ViewEventListener> listeners = new ArrayList<>();

  public WizardStepView(List<E> data) {
    this.backend = Collections.unmodifiableList(data);
    this.idx=0;
  }

  public E current() {
    return this.backend.get(this.idx);
  }

  public boolean canBackward() {
    return (this.idx > 0);
  }


  public void backward() throws IllegalStateException {
    if (this.idx == 0) {
      throw new IllegalStateException("cannot move backward");
    }
    this.idx--;
    this.listeners.forEach(listener -> {
      listener.notifyViewChange();
    });
  }

  public boolean canForward() {
    return (this.idx < this.backend.size() - 1);
  }

  public void forward() throws IllegalStateException  {
    if (this.idx == this.backend.size() -1 ) {
      throw new IllegalStateException("cannot move forwad");
    }
    this.idx++;
    this.listeners.forEach(listener -> {
      listener.notifyViewChange();
    });
  }

  public int progress() {
    return (int) Math.abs((((float)this.idx) / ((float)this.backend.size())) * 100.0);
  }

  @Override public String toString() {
    return "WizardStepView{" + "size="+ this.backend.size() +", idx=" + idx + ", current=" +this.backend.get(this.idx)+'}';
  }

  public void addListener(ViewEventListener listener) {
    this.listeners.add(listener);
  }
}
