package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service class responsible for managing AI credentials.
 * Provides functionality to asynchronously list detailed information about credentials stored in the database.
 */
public class AICredentialService {

  private final ConnectionRef connectionRef;


  /**
   * Constructs a new AICredentialService with a specified connection handler.
   *
   * @param connectionRef The connection reference for the connection handler responsible for managing database connections
   *                          and interactions.
   */
  public AICredentialService(ConnectionRef connectionRef) {
    this.connectionRef = connectionRef;
  }

  /**
   * Asynchronously creates a new credential.
   */
  public CompletableFuture<Void> createCredential(CredentialProvider credentialProvider) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        connectionRef.get().getOracleAIInterface().createCredential(connection, credentialProvider);
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException(e);
      }
    });
  }

  /**
   * Asynchronously updates a attributes of existing credential.
   */
  public CompletableFuture<Void> updateCredential(CredentialProvider credentialProvider) {
    return CompletableFuture.supplyAsync(() -> {
//      try {
//        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
////        connectionHandler.getOracleAIInterface().setCredentialAttribute(connection, credentialProvider.getCredentialName(), credentialProvider.getUsername());
//      } catch (CredentialManagementException | SQLException e) {
//        throw new CompletionException(e);
//      }
      return null;
    });
  }
  /**
   * Asynchronously lists detailed credential information from the database.
   */
  public CompletableFuture<CredentialProvider[]> listCredentials() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Obtain a connection for Oracle AI session
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);

        List<CredentialProvider> credentialProviderList = connectionRef.get().getOracleAIInterface().listCredentialsDetailed(connection);
        // Fetch and return detailed list of credentials
        return credentialProviderList.toArray(CredentialProvider[]::new);
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException(e);
      }
    });
  }


  /**
   * Asynchronously deletes a specific credential information from the database.
   */
  public CompletableFuture<Void> deleteCredential(String credentialName) {
    if(credentialName.isEmpty()){
      throw new IllegalArgumentException("Credential Name shouldn't be empty");
    }
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        connectionRef.get().getOracleAIInterface().dropCredential(connection, credentialName);
      } catch (SQLException | CredentialManagementException e) {
        throw new CompletionException(e);
      }
    });
  }
}
