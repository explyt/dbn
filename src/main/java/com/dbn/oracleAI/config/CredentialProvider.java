package com.dbn.oracleAI.config;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * This class can store credentials for basic
 * authentication with a username and password, as well as for more complex
 * authentications like those required for OCI services.
 * This class implements the {@link AttributeInput} interface, which requires validation
 * and formatting methods suitable for use in preparing data for PL/SQL calls.
 */

@Getter
@ToString
public class CredentialProvider implements AttributeInput {

  protected String credentialName;
  protected String username;
  protected List<Profile> profiles = new ArrayList<>();

  public CredentialProvider(String credentialName, String userName) {
    this.credentialName = credentialName;
    this.username = userName;
  }

  public CredentialProvider(String credentialName) {
    this.credentialName = credentialName;
  }

}
