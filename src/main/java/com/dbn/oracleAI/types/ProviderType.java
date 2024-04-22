package com.dbn.oracleAI.types;

import lombok.Getter;

/**
 * This enum is for listing the possible credential providers we have
 */
@Getter
public enum ProviderType {

  COHERE,
  OPENAI,
  OCI;

}
