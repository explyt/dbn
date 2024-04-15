package com.dbn.oracleAI.types;

import lombok.Getter;

/**
 * This enum is for listing the possible ways of creating a new credential provider
 */
@Getter
public enum CredentialType {
  PASSWORD("password"),
  OCI("oci");

  private final String action;

  CredentialType(String action) {
    this.action = action;
  }
}
