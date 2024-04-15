package com.dbn.oracleAI.config;

public class PasswordCredentialProvider extends CredentialProvider {
  private final String password;

  public PasswordCredentialProvider(String credentialName, String username, String password) {
    super(credentialName, username);
    this.password = password;
    validate();
  }

  /**
   * validate that the fields aren't empty and that they don't contain "'"
   */
  @Override
  public void validate() {
    if (credentialName.isEmpty() || username.isEmpty() || password.isEmpty()) throw new IllegalArgumentException("Please don't leave empty fields");
    if (credentialName.contains("'") || username.contains("'") || password.contains("'")) throw new IllegalArgumentException("Please don't use ' in fields");
  }

  /**
   * Give us a format suitable to be injected in our pl/sql calls
   * @return string of attributes
   */
  @Override
  public String toAttributeMap() {
    return String.format(
        "credential_name => '%s',\n" +
            "username => '%s',\n" +
            "password => '%s'",
        credentialName,
        username,
        password
    );
  }
}
