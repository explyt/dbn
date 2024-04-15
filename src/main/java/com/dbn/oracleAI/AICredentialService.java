package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;
import com.intellij.openapi.application.ApplicationManager;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service class responsible for managing AI credentials.
 * Provides functionality to asynchronously list detailed information about credentials stored in the database.
 */
public class AICredentialService {

  private final ConnectionHandler connectionHandler;
  private ConcurrentMap<String, CredentialProvider> credentialsProvidersMap;


  /**
   * Constructs a new AICredentialService with a specified connection handler.
   *
   * @param connectionHandler The connection handler responsible for managing database connections
   *                          and interactions.
   */
  public AICredentialService(ConnectionHandler connectionHandler) {
    this.connectionHandler = connectionHandler;
  }

  /**
   * Asynchronously creates a new credential.
   */
  public CompletableFuture<Void> createCredential(CredentialProvider credentialProvider) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
        connectionHandler.getOracleAIInterface().createCredential(connection, credentialProvider);
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException(e);
      }
      return null;
    });
  }
  /**
   * Asynchronously lists detailed credential information from the database.
   * This method fetches credentials using the specified Oracle AI session and returns a list
   * of {@link CredentialProvider} objects containing detailed credential information.
   */
  public CompletableFuture<CredentialProvider[]> listCredentials() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Obtain a connection for Oracle AI session
        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);

        List<CredentialProvider> credentialProviderList = connectionHandler.getOracleAIInterface().listCredentialsDetailed(connection);
        ApplicationManager.getApplication().invokeLater(() -> {
          credentialsProvidersMap = credentialProviderList.stream()
              .collect(Collectors.toConcurrentMap(CredentialProvider::getCredentialName, Function.identity()));
        });
        // Fetch and return detailed list of credentials
        return credentialProviderList.toArray(CredentialProvider[]::new);
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException(e);
      }
    });
  }


  /**
   * Asynchronously deletes a specific credential information from the database.
   * This method drops a credential using its name.
   *
   * @param credentialName the name of the credential we want to delete
   */
  public CompletableFuture<Void> deleteCredential(String credentialName) {
    if(credentialName.isEmpty()){
      throw new IllegalArgumentException("Credential Name shouldn't be empty");
    }
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
        connectionHandler.getOracleAIInterface().dropCredential(connection, credentialName);
        credentialsProvidersMap.remove(credentialName);
      } catch (SQLException | CredentialManagementException e) {
        throw new CompletionException(e);
      }
    });
  }
}
