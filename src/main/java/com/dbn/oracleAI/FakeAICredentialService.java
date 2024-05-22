package com.dbn.oracleAI;

import com.dbn.oracleAI.config.Credential;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FakeAICredentialService implements AICredentialService {

  Type CREDENTIAL_TYPE = new TypeToken<List<Credential>>() {
  }.getType();
  String credentialsRepoFilename = System.getProperty("fake.services.credential.dump", "/var/tmp/credentials.json");
  //keep track of profiles
  // no synch needed
  List<Credential> credentials = null;


  @Override
  public CompletableFuture<Void> createCredential(Credential credential) {
    this.credentials.add(credential);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> updateCredential(Credential credential) {
    this.credentials.removeIf(cred -> cred.getCredentialName().equalsIgnoreCase(credential.getCredentialName()));
    this.credentials.add(credential);
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<List<Credential>> getCredentials() {
    if (credentials == null) {
      try {
        this.credentials = new Gson().fromJson(new FileReader(credentialsRepoFilename), CREDENTIAL_TYPE);
      } catch (FileNotFoundException e) {
        throw new RuntimeException("cannot read credentials  list " + e.getMessage());
      }
    }
    return CompletableFuture.completedFuture(this.credentials);
  }

  @Override
  public CompletableFuture<Void> deleteCredential(String credentialName) {
    this.credentials.removeIf(cred -> cred.getCredentialName().equalsIgnoreCase(credentialName));
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void updateStatus(String credentialName, Boolean isEnabled) {
  }
}
