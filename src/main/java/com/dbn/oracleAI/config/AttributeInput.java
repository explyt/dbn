package com.dbn.oracleAI.config;

public interface AttributeInput {

  /**
   * Validates the information by making sure the required attributes are not empty
   * and don't have illegal characters
   */
  void validate();


  /**
   * Formats the attributes into a string suitable for PL/SQL calls.
   *
   * @param forCreation This is to specify whether the resulting output is for creating a new profile or updating an existing one
   */
  String toAttributeMap(boolean forCreation);

}
