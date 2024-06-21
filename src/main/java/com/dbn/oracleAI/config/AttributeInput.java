package com.dbn.oracleAI.config;

public interface AttributeInput {

  /**
   * Validates the information by making sure the required attributes are not empty
   * and don't have illegal characters
   */
  void validate();


  /**
   * Formats the attributes into a string suitable for PL/SQL calls.
   **/
  String toAttributeMap();

  /**
   * Gets unique identifier
   * @return the uuid
   */
  String getUuid();

}
