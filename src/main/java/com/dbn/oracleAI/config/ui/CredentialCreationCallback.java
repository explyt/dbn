package com.dbn.oracleAI.config.ui;

/**
 * Allows us to define callback method to define the behaviour we want when creating a new credential
 * i.e. refreshing list of credentials
 */
public interface CredentialCreationCallback {
  void onCredentialCreated();
}
