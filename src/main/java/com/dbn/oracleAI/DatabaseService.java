package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.oracleAI.config.DBObjectItem;
import com.intellij.openapi.project.Project;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Database information service
 */
public interface DatabaseService {
    /**
     * Loads all schemas that are accessible for the current user asynchronously
     */
    CompletableFuture<List<String>> getSchemaNames();

    /**
     * Loads all object list items accessible to the user from a specific schema asynchronously
     */
    CompletableFuture<List<DBObjectItem>> getObjectItemsForSchema(String schema);

    CompletionStage<Void> grantACLRights(String command);

    CompletionStage<Void> grantPrivilege(String username);

    CompletionStage<Void> isUserAdmin();

    static DatabaseService getInstance(ConnectionHandler connection) {
        Project project = connection.getProject();
        ConnectionId connectionId = connection.getConnectionId();
        DatabaseOracleAIManager manager = DatabaseOracleAIManager.getInstance(project);
        return manager.getDatabaseService(connectionId);
    }

}
