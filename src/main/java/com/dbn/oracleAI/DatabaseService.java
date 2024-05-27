package com.dbn.oracleAI;

import com.dbn.oracleAI.config.DBObjectItem;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
}
