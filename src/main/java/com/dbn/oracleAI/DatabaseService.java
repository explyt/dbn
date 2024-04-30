package com.dbn.oracleAI;

import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service to handle DB objects and operations
 */
public class DatabaseService {

    private static final Logger LOGGER = Logger.getInstance(DatabaseService.class.getPackageName());

    private final ConnectionRef connectionRef;

    DatabaseService(ConnectionRef connectionRef) {
        assert connectionRef.get() != null : "No connection";
        this.connectionRef = connectionRef;
    }

    /**
     * Loads all schemas that are accessible for the current user asynchronously
     */
    public CompletableFuture<List<String>> getSchemaNames() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOGGER.debug("fetching schemas");
                DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
                List<String> schemas = connectionRef.get().getOracleAIInterface().listSchemas(connection);
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("fetched schemas: " + schemas);
                return schemas;
            } catch (DatabaseOperationException | SQLException e) {
                LOGGER.error("cannot fetch schemas", e);
                throw new CompletionException("Cannot get schemas", e);
            }
        });
    }


    /**
     * Loads all object list items accessible to the user from a specific schema asynchronously
     */
    public CompletableFuture<List<DBObjectItem>> getObjectItemsForSchema(String schema) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOGGER.debug("fetching objects for schema " + schema);
                DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
                List<DBObjectItem> objectListItemsList = connectionRef.get().getOracleAIInterface().listObjectListItems(connection, schema);
                LOGGER.debug(objectListItemsList.size() + " objects returned");
                return objectListItemsList;
            } catch (DatabaseOperationException | SQLException e) {
                LOGGER.error("error while fetching schema object list",e);
                throw new CompletionException("Cannot list object list items", e);
            }
        });
    }

}
