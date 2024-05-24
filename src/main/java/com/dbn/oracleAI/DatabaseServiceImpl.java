package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.diagnostic.Logger;

import java.io.FileWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service to handle DB objects and operations
 */
public class DatabaseServiceImpl implements DatabaseService {

    private static final Logger LOGGER = Logger.getInstance(DatabaseServiceImpl.class.getPackageName());

    private final ConnectionRef connectionRef;

    public ConnectionHandler getCnxH() {
        return connectionRef.get();
    }

    public CompletableFuture<List<String>> getSchemaNames() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DatabaseServiceImpl.LOGGER.debug("fetching schemas");
                DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
                List<String> schemas = connectionRef.get().getOracleAIInterface().listSchemas(connection);

                if (DatabaseServiceImpl.LOGGER.isDebugEnabled())
                    DatabaseServiceImpl.LOGGER.debug("fetched schemas: " + schemas);
                if (System.getProperty("fake.services.schemas.dump") != null) {
                    try {
                        FileWriter writer = new FileWriter(System.getProperty("fake.services.schemas.dump"));
                        new Gson().toJson(schemas, writer);
                        writer.close();
                    } catch (Exception e) {
                        // ignore this
                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("cannot dump schemas " +e.getMessage());
                    }
                }
                return schemas;
            } catch (DatabaseOperationException | SQLException e) {
                DatabaseServiceImpl.LOGGER.error("cannot fetch schemas", e);
                throw new CompletionException("Cannot get schemas", e);
            }
        });
    }
    public CompletableFuture<List<DBObjectItem>> getObjectItemsForSchema(String schema) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOGGER.debug("getObjectItemsForSchema => fetching objects for schema " + schema);
                DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
                List<DBObjectItem> objectListItemsList = connectionRef.get().getOracleAIInterface().listObjectListItems(connection, schema);
                LOGGER.debug("getObjectItemsForSchema => "+ objectListItemsList.size() + " objects returned");
                if (System.getProperty("fake.services.dbitems.dump") != null) {
                    try {
                        FileWriter writer = new FileWriter(System.getProperty("fake.services.dbitems.dump"),true);
                        writer.write(schema);
                        writer.write(':');
                        new GsonBuilder().setLenient().create().toJson(objectListItemsList, writer);
                        writer.write('\n');
                        writer.close();
                    } catch (Exception e) {
                        // ignore this
                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("Cannot dump obj list" +e.getMessage());
                    }
                }
                return objectListItemsList;
            } catch (DatabaseOperationException | SQLException e) {
                DatabaseServiceImpl.LOGGER.error("error while fetching schema object list", e);
                throw new CompletionException("Cannot list object list items", e);
            }
        });
    }
    DatabaseServiceImpl(ConnectionRef connectionRef) {
        assert connectionRef.get() != null : "No connection";
        this.connectionRef = connectionRef;
    }


}
