package com.dbn.oracleAI;

import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.types.ProviderType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


/**
 * Holder class for profile combox box
 */
@Getter
@Setter
@EqualsAndHashCode
public final class AIProfileItem {
  /**
   * Creates a new combo item
   *
   * @param label the label to be displayed in the combo
   */
  public AIProfileItem(String label, ProviderType provider, ProviderModel model, boolean isEnabled) {
    this.label = label;
    this.provider = provider;
    this.model = model;
    this.isEnabled = isEnabled;
    this.effective = true;
  }

  /**
   * Creates a new combo item
   *
   * @param label     the label
   * @param effective is this effective or placeholder item ?
   */
  public AIProfileItem(String label, boolean effective) {
    this.label = label;
    this.effective = effective;
  }


  /**
   * Used to UI fw
   *
   * @return the label
   */
  @Override
  public String toString() {
    return label;
  }

  /**
   * the label of this combo item
   */
  private String label;
  private ProviderType provider;
  private ProviderModel model;
  private boolean isEnabled = true;

  /**
   * Checks that this is effective/usable profile
   * basic example is that the 'New profile...' is not
   */
  private boolean effective;

}

/**
 * Dedicated class for NL2SQL profile model.
 */
