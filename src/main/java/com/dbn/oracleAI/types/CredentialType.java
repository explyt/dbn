package com.dbn.oracleAI.types;


/**
 * This enum is for listing the possible ways of creating a new credential
 */
public enum CredentialType {
  /**
   * We can create either using username/password aka the provider key, or we can use OCI information
   */
  PASSWORD,
  OCI;

}
