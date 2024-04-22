package com.dbn.oracleAI.config;

import lombok.Getter;
import lombok.ToString;

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
public class Credential implements AttributeInput {

  protected String credentialName;
  protected String username;
  protected boolean isEnabled;
  protected String comments;

  public Credential(String credentialName, String userName, boolean isEnabled, String comments) {
    this.credentialName = credentialName;
    this.username = userName;
    this.isEnabled = isEnabled;
    this.comments = comments;
  }

  public List<String> toUpdatingAttributeList() {
    return null;
  }

  ;

  protected String toAttributeFormat(String attributeName, String attributeValue) {
    return String.format(
        "credential_name => '%s', \n" +
            "attribute => '%s', \n" +
            "value => '%s'",
        credentialName,
        attributeName,
        attributeValue
    );
  }

}
