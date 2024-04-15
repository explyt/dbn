package com.dbn.oracleAI.config;


public class OciCredentialProvider extends CredentialProvider {
  protected String userTenancyOCID;
  protected String privateKey;
  protected String fingerprint;

  public OciCredentialProvider(String credentialName, String userOcid, String tenancyOcid, String privateKey, String fingerprint) {
    super(credentialName, userOcid);
    this.userTenancyOCID = tenancyOcid;
    this.privateKey = privateKey;
    this.fingerprint = fingerprint;
    validate();
  }

  /**
   * validate that the fields aren't empty and that they don't contain "'"
   */
  @Override
  public void validate() {
    if (credentialName.isEmpty() || username.isEmpty() || userTenancyOCID.isEmpty() || privateKey.isEmpty() || fingerprint.isEmpty()) throw new IllegalArgumentException("Please don't leave empty fields");
    if (credentialName.contains("'") || username.contains("'") || userTenancyOCID.contains("'") || privateKey.contains("'") || fingerprint.contains("'")) throw new IllegalArgumentException("Please don't use ' in fields");
  }

  /**
   * Give us a format suitable to be injected in our pl/sql calls
   * @return string of attributes
   */
  @Override
  public String toAttributeMap() {
    return String.format(
        "credential_name => '%s', \n" +
            "user_ocid => '%s', \n" +
            "tenancy_ocid => '%s', \n" +
            "private_key => '%s', \n" +
            "fingerprint => '%s'",
        credentialName,
        username,
        userTenancyOCID,
        privateKey,
        fingerprint
    );
  }
}

