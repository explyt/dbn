package com.dbn.oracleAI;

import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.Credential;
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
   *                      and interactions.
   * @throws CredentialManagementException
   */
  public AICredentialService(ConnectionRef connectionRef) {
    assert connectionRef != null : "No connection";
    this.connectionRef = connectionRef;
  }

  /**
   * Asynchronously creates a new credential.
   *
   * @throws CredentialManagementException underlying service failed
   */
  public CompletableFuture<Void> createCredential(Credential credential) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        connectionRef.get().getOracleAIInterface().createCredential(connection, credential);
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException("Cannot create credential", e);
      }
    });
  }

  /**
   * Asynchronously updates an attributes of existing credential.
   *
   * @throws CredentialManagementException
   */
  public CompletableFuture<Void> updateCredential(Credential credential) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        connectionRef.get().getOracleAIInterface().setCredentialAttribute(connection, credential);
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException("Cannot update credential", e);
      }
    });
  }

  /**
   * Asynchronously lists detailed credential information from the database.
   *
   * @throws CredentialManagementException
   */
  public CompletableFuture<List<Credential>> getCredentials() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Obtain a connection for Oracle AI session
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);

        List<Credential> credentialList = connectionRef.get().getOracleAIInterface().listCredentials(connection);
        // Fetch and return detailed list of credentials
        return credentialList;
      } catch (CredentialManagementException | SQLException e) {
        throw new CompletionException("Cannot list credentials", e);
      }
    });
  }


  /**
   * Asynchronously deletes a specific credential information from the database.
   *
   * @throws CredentialManagementException
   */
  public CompletableFuture<Void> deleteCredential(String credentialName) {
    if (credentialName.isEmpty()) {
      throw new IllegalArgumentException("Credential Name shouldn't be empty");
    }
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        connectionRef.get().getOracleAIInterface().dropCredential(connection, credentialName);
      } catch (SQLException | CredentialManagementException e) {
        throw new CompletionException("Cannot delete credential", e);
      }
    });
  }
}
