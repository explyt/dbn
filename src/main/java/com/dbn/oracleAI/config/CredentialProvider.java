package com.dbn.oracleAI.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CredentialProvider implements AttributeInput {
  protected String credentialName;
  protected String username;
  protected List<Profile> profiles = new ArrayList<>();

  public CredentialProvider(String credentialName) {
    this.credentialName = credentialName;
  }
  public CredentialProvider(String credentialName, String username) {
    this.credentialName = credentialName;
    this.username = username;
  }

  @Override
  public boolean isValid() {
    return credentialName != null && !credentialName.contains("'");
  }

  @Override
  public String format(){
    return "";
  };
}
