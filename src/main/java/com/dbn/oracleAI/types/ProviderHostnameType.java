package com.dbn.oracleAI.types;


import lombok.Getter;

/**
 * This enum is for listing the possible credential providers we have
 */
@Getter
public enum ProviderHostnameType {
  COHERE("api.cohere.ai"),
  OPENAI("api.openai.com"),
  //TODO find the actual value
  OCI("api.oci.com");

  private final String hostname;

  ProviderHostnameType(String hostname) {
    this.hostname = hostname;
  }
}
