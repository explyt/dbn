package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Service to handle DB objects and operations
 */
@Slf4j
public class DatabaseServiceImpl extends AIAssistantComponent implements DatabaseService {


  DatabaseServiceImpl(ConnectionHandler connection) {
    super(connection);
  }

  public CompletableFuture<List<String>> getSchemaNames() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        log.debug("fetching schemas");
        DBNConnection connection = getAssistantConnection();
        List<String> schemas = getAssistantInterface().listSchemas(connection);
        if (log.isDebugEnabled())
          log.debug("fetched schemas: " + schemas);
        if (System.getProperty("fake.services.schemas.dump") != null) {
          try {
            FileWriter writer = new FileWriter(System.getProperty("fake.services.schemas.dump"));
            new Gson().toJson(schemas, writer);
            writer.close();
          } catch (Exception e) {
            // ignore this
            if (log.isTraceEnabled())
              log.trace("cannot dump schemas " + e.getMessage());
          }
        }
        return schemas;
      } catch (DatabaseOperationException | SQLException e) {
        log.warn("cannot fetch schemas", e);
        throw new CompletionException("Cannot get schemas", e);
      }
    });
  }

  public CompletableFuture<List<DBObjectItem>> getObjectItemsForSchema(String schema) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        log.debug("fetching objects for schema " + schema);
        DBNConnection connection = getAssistantConnection();
        List<DBObjectItem> objectListItemsList = getAssistantInterface().listObjectListItems(connection, schema);
        log.debug("getObjectItemsForSchema: "+objectListItemsList.size() + " objects returned ");
        if (System.getProperty("fake.services.dbitems.dump") != null) {
          try {
            FileWriter writer = new FileWriter(System.getProperty("fake.services.dbitems.dump"), true);
            writer.write(schema);
            writer.write(':');
            new GsonBuilder().setLenient().create().toJson(objectListItemsList, writer);
            writer.write('\n');
            writer.close();
          } catch (Exception e) {
            // ignore this
            if (log.isTraceEnabled())
              log.trace("Cannot dump obj list" + e.getMessage());
          }
        }
        return objectListItemsList;
      } catch (DatabaseOperationException | SQLException e) {
        log.warn("error while fetching schema object list", e);
        throw new CompletionException("Cannot list object list items", e);
      }
    });
  }

  public CompletableFuture<Void> grantACLRights(String command) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection connection = getAssistantConnection();
        getAssistantInterface().grantACLRights(connection, command);
      } catch (SQLException e) {
        throw new CompletionException(e);
      }
      return null;
    });
  }

  public CompletableFuture<Void> grantPrivilege(String username) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection connection = getAssistantConnection();
        getAssistantInterface().grantPrivilege(connection, username);
      } catch (SQLException e) {
        throw new CompletionException(e);
      }
      return null;
    });
  }

  public CompletableFuture<Void> isUserAdmin() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection connection = getAssistantConnection();
        getAssistantInterface().checkAdmin(connection);
      } catch (SQLException e) {
        throw new CompletionException(e);
      }
      return null;
    });
  }
}
