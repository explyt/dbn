package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service class responsible for managing AI credentials.
 * Provides functionality to asynchronously list detailed information about credentials stored in the database.
 */
public class AICredentialService {

  private final ConnectionHandler connectionHandler;

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
   * Asynchronously creates a new password-based credential.
   * This method is intended for credentials
   * that use a username and password for authentication.
   *
   * @param credentialName The name of the credential to create. This name is used to uniquely identify the credential.
   * @param username The username associated with the credential which is the username in the AI Provider.
   * @param password The password associated with the credential which is the key for the AI Provider.
   */

  public CompletableFuture<Void> createPasswordCredential(String credentialName, String username, String password){
    CredentialProvider credentialProvider = CredentialProvider.builder().credentialName(credentialName).username(username).password(password).build();
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
        connectionHandler.getOracleAIInterface().createCredential(connection, credentialProvider);
      } catch (SQLException | CredentialManagementException e) {
        throw new RuntimeException(e);
      }
      return null;
    });
  }

  /**
   * Asynchronously creates a new Oracle Cloud Infrastructure (OCI) credential.
   * This method creates a credential with the specified name, user OCID, user tenancy OCID, private key,
   * and fingerprint.
   * This method is intended for credentials that use Oracle Cloud Infrastructure for authentication.
   *
   * @param credentialName The name of the credential to create. This name is used to uniquely identify the credential.
   * @param userOCID The Oracle Cloud Identifier (OCID) for the user.
   * @param userTenancyOCID The OCID of the user's tenancy.
   * @param privateKey The private key associated with the OCI credential.
   * @param fingerprint The fingerprint associated with the OCI credential's public key.
   */

  public CompletableFuture<Void> createOCICredential(String credentialName, String userOCID, String userTenancyOCID, String privateKey, String fingerprint){
    CredentialProvider credentialProvider = CredentialProvider.builder().credentialName(credentialName).userOCID(userOCID).userTenancyOCID(userTenancyOCID).privateKey(privateKey).fingerprint(fingerprint).build();
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
        connectionHandler.getOracleAIInterface().createCredential(connection, credentialProvider);
      } catch (SQLException | CredentialManagementException e) {
        throw new RuntimeException(e);
      }
      return null;
    });
  }
  /**
   * Asynchronously lists detailed credential information from the database.
   * This method fetches credentials using the specified Oracle AI session and returns a list
   * of {@link CredentialProvider} objects containing detailed credential information.
   *
   * @return A CompletableFuture that, when completed, returns a list of CredentialProvider objects
   *         containing detailed information about each credential stored in the database.
   *         The future may complete exceptionally with a RuntimeException if there is an issue
   *         with database operation or SQL execution.
   */
  public CompletableFuture<List<CredentialProvider>> listCredentialsDetailed(){
    return CompletableFuture.supplyAsync(() -> {
      try {
        // Obtain a connection for Oracle AI session
        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
        // Fetch and return detailed list of credentials
        return connectionHandler.getOracleAIInterface().listCredentialsDetailed(connection);
      } catch (DatabaseOperationException | SQLException e) {
        System.out.println(e);
        throw new RuntimeException(e);
      }
    });
  }

  /**
   * Asynchronously deletes a specific credential information from the database.
   * This method drops a credential using its name.
   * @param credentialName the name of the credential we want to delete
   */
  public CompletableFuture<Void> deleteCredential(String credentialName) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
        connectionHandler.getOracleAIInterface().dropCredential(connection, credentialName);
      } catch (SQLException | CredentialManagementException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    });
  }
}
