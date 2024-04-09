package com.dbn.oracleAI.config;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class CredentialProvider implements AttributeInput {
  protected String credentialName;
  protected String username;
  protected String password;
  protected String userOCID;
  protected String userTenancyOCID;
  protected String privateKey;
  protected String fingerprint;
  protected List<Profile> profiles = new ArrayList<>();

  @Override
  public boolean isValid() {
    return credentialName != null && !credentialName.contains("'");
  }

  /**
   * This is to add these attribute in the corresponding pl/sql call
   */
  @Override
  public String format(){
    if (!isValid()) {
      throw new IllegalArgumentException("Invalid credential attributes.");
    }
    if(username != null){

      return String.format(
          "credential_name => '%s',\n" +
              "username => '%s',\n" +
              "password => '%s'",
          credentialName,
          username,
          password
      );
    } else {
      return String.format(
          "credential_name => '%s', \n" +
              "user_ocid => '%s', \n" +
              "tenancy_ocid => '%s' \n" +
              "private_key => '%s' \n" +
              "fingerprint => '%s'",
          credentialName,
          userOCID,
          userTenancyOCID,
          privateKey,
          fingerprint
      );

    }

  };
}
