//package com.dbn.oracleAI.config;
//
//public class OciCredentialProvider extends CredentialProvider {
//  private final String userOcid;
//  private final String tenancyOcid;
//  private final String privateKey;
//  private final String fingerprint;
//
//  public OciCredentialProvider(String credentialName, String userOcid, String tenancyOcid, String privateKey, String fingerprint) {
//    super(credentialName);
//    this.userOcid = userOcid;
//    this.tenancyOcid = tenancyOcid;
//    this.privateKey = privateKey;
//    this.fingerprint = fingerprint;
//  }
//
//  @Override
//  public boolean isValid() {
//    if (!super.isValid()) return false;
//    if (userOcid != null && userOcid.contains("'")) return false;
//    if (tenancyOcid != null && tenancyOcid.contains("'")) return false;
//    if (privateKey != null && privateKey.contains("'")) return false;
//    return fingerprint != null && !fingerprint.contains("'");
//  }
//
//  @Override
//  public String format() {
//      if (!isValid()) {
//        throw new IllegalArgumentException("Invalid credential attributes.");
//      }
//      return String.format(
//
//          "       credential_name => '%s',\n" +
//              "       user_ocid       => '%s',\n" +
//              "       tenancy_ocid    => '%s',\n" +
//              "       private_key     => '%s',\n" +
//              "       fingerprint     => '%s'",
//          credentialName, userOcid, tenancyOcid, privateKey, fingerprint);
//  }
//}
//
