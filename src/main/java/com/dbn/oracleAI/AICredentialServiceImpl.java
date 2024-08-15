package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service class responsible for managing AI credentials.
 * Provides functionality to asynchronously list detailed information about credentials stored in the database.
 */
@Slf4j
public class AICredentialServiceImpl extends AIAssistantComponent implements AICredentialService {

  /**
   * Constructs a new AICredentialService with a specified connection handler.
   *
   * @param connection The {@link ConnectionHandler} responsible for managing database connections and interactions.
   */
  public AICredentialServiceImpl(ConnectionHandler connection) {
    super(connection);
  }

  @Override
  public CompletableFuture<Void> createCredential(Credential credential) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = getAssistantConnection();
        getAssistantInterface().createCredential(connection, credential);
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException("Cannot create credential", e);
      }
    });
  }

  @Override
  public CompletableFuture<Void> updateCredential(Credential editedCredential) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = getAssistantConnection();
        getAssistantInterface().setCredentialAttribute(connection, editedCredential);
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException("Cannot update credential", e);
      }
    });
  }

  @Override
  public CompletableFuture<List<Credential>> getCredentials() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Obtain a connection for Oracle AI session
        DBNConnection connection = getAssistantConnection();

        List<Credential> credentialList = getAssistantInterface().listCredentials(connection);
        if (System.getProperty("fake.services.credentials.dump") != null) {
          try {
            FileWriter writer = new FileWriter(System.getProperty("fake.services.credentials.dump"));
            new Gson().toJson(credentialList, writer);
            writer.close();
          } catch (Exception e) {
            // ignore this
            if (log.isTraceEnabled())
              log.trace("cannot dump profile " + e.getMessage());
          }
        }
        // Fetch and return detailed list of credentials
        return credentialList;
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException("Cannot list credentials", e);
      }
    });
  }

  @Override
  public CompletableFuture<Void> deleteCredential(String credentialName) {
    if (credentialName.isEmpty()) {
      throw new IllegalArgumentException("Credential Name shouldn't be empty");
    }
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = getAssistantConnection();
        getAssistantInterface().dropCredential(connection, credentialName);
      } catch (SQLException | CredentialManagementException e) {
        throw new CompletionException("Cannot delete credential", e);
      }
    });
  }

  @Override
  public void updateStatus(String credentialName, Boolean isEnabled) {
    if (credentialName.isEmpty()) {
      throw new IllegalArgumentException("Credential Name shouldn't be empty");
    }
    CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = getAssistantConnection();
        getAssistantInterface().updateCredentialStatus(connection, credentialName, isEnabled);
      } catch (SQLException | CredentialManagementException e) {
        throw new CompletionException("Cannot update credential status", e);
      }
    });
  }
}
