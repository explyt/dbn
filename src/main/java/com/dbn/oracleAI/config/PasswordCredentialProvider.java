//package com.dbn.oracleAI.config;
//
//public class PasswordCredentialProvider extends CredentialProvider {
//  private final String username;
//  private final String password;
//
//  public PasswordCredentialProvider(String credentialName, String username, String password) {
//    super(credentialName);
//    this.username = username;
//    this.password = password;
//  }
//
//  @Override
//  public boolean isValid() {
//    if (!super.isValid()) return false;
//    if (username != null && username.contains("'")) return false;
//    return password != null && !password.contains("'");
//  }
//
//  @Override
//  public String format() {
//    if (!isValid()) {
//      return "Invalid input parameters.";
//    }
//    return String.format(
//            "    credential_name => '%s',\n" +
//            "    username => '%s',\n" +
//            "    password => '%s' );\n",
//            credentialName, username, password);
//  }
//}
