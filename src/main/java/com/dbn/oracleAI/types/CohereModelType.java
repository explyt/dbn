package com.dbn.oracleAI.types;

import lombok.Getter;

/**
 * Enumerates cohere models
 */
@Getter
public enum CohereModelType {
  DEFAULT_COHERE("command"),
  COMMAND_NIGHTLY("command-nightly"),
  COMMAND_LIGHT("command-light"),
  COMMAND_LIGHT_NIGHTLY("command-light-nightly");


  private final String action;

  CohereModelType(String action) {
    this.action = action;
  }
}
