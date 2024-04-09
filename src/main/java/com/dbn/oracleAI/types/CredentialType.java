package com.dbn.oracleAI.types;

import lombok.Getter;

@Getter
public enum CredentialType {
  PASSWORD("password"),
  OCI("oci");

  private final String action;

  CredentialType(String action) {
    this.action = action;
  }
}
