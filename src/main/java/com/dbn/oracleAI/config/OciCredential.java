package com.dbn.oracleAI.config;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class OciCredential extends Credential {
  private final String userTenancyOCID;
  private final String privateKey;
  private final String fingerprint;

  public OciCredential(String credentialName, String userOcid, String tenancyOcid, String privateKey, String fingerprint) {
    super(credentialName, userOcid, true, null);
    this.userTenancyOCID = tenancyOcid;
    this.privateKey = privateKey;
    this.fingerprint = fingerprint;
    validate();
  }

  /**
   * validate that the fields aren't empty and that they don't contain "'"
   *
   * @throws IllegalArgumentException when the rules of validation are not respected
   */
  @Override
  public void validate() {
    if (credentialName.isEmpty() || username.isEmpty() || userTenancyOCID.isEmpty() || privateKey.isEmpty() || fingerprint.isEmpty())
      throw new IllegalArgumentException("Please don't leave empty fields");
    if (credentialName.contains("'") || username.contains("'") || userTenancyOCID.contains("'") || privateKey.contains("'") || fingerprint.contains("'"))
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

  @Override
  public List<String> toUpdatingAttributeList() {
    List<String> output = new ArrayList<>();

    if (!Objects.equals(username, "")) output.add(toAttributeFormat("user_ocid", username));
    if (!Objects.equals(userTenancyOCID, "")) output.add(toAttributeFormat("user_tenancy_ocid", userTenancyOCID));
    if (!Objects.equals(privateKey, "")) output.add(toAttributeFormat("private_key", privateKey));
    if (!Objects.equals(fingerprint, "")) output.add(toAttributeFormat("fingerprint", fingerprint));
    return output;
  }


}

