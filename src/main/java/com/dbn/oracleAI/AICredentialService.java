package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;

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
}
