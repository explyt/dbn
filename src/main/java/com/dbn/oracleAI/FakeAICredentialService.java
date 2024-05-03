package com.dbn.oracleAI;

import com.dbn.oracleAI.config.Credential;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FakeAICredentialService implements AICredentialService {

    Type PROFILE_TYPE = new TypeToken<List<Credential>>() {
    }.getType();
    String profilesRepoFilename = System.getProperty("fake.services.credential.dump", "/var/tmp/credential.json");
    //keep track of profiles
    // no synch needed
    List<Credential> profiles = null;


    @Override
    public CompletableFuture<Void> createCredential(Credential credential) {
        return null;
    }

    @Override
    public CompletableFuture<Void> updateCredential(Credential credential) {
        return null;
    }

    @Override
    public CompletableFuture<List<Credential>> getCredentials() {
        return null;
    }

    @Override
    public CompletableFuture<Void> deleteCredential(String credentialName) {
        return null;
    }
}
