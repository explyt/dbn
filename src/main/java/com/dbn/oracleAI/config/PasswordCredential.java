package com.dbn.oracleAI.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PasswordCredential extends Credential {
  private final String password;

  public PasswordCredential(String credentialName, String username, String password) {
    super(credentialName, username, true, null);
    this.password = password;
    validate();
  }

  /**
   * validate that the fields aren't empty and that they don't contain "'"
   *
   * @throws IllegalArgumentException when the rules of validation are not respected
   */
  @Override
  public void validate() {
    if (credentialName.isEmpty() || username.isEmpty() || password.isEmpty())
      throw new IllegalArgumentException("Please don't leave empty fields");
    if (credentialName.contains("'") || username.contains("'") || password.contains("'"))
      throw new IllegalArgumentException("Please don't use ' in fields");
  }

  /**
   * Give us a format suitable to be injected in our pl/sql calls
   *
   * @return string of attributes
   */
  @Override
  public String toAttributeMap(boolean forCreation) {
    return String.format(
        "credential_name => '%s',\n" +
            "username => '%s',\n" +
            "password => '%s'",
        credentialName,
        username,
        password
    );
  }

  @Override
  public List<String> toUpdatingAttributeList() {
    List<String> output = new ArrayList<>();

    if (!Objects.equals(username, "")) output.add(toAttributeFormat("username", username));
    if (!Objects.equals(password, "")) output.add(toAttributeFormat("password", password));
    return output;
  }
}
