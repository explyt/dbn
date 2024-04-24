package com.dbn.oracleAI;

import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.ObjectListItem;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.dbn.oracleAI.types.ProviderType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Service to handle AI profiles
 */
public class AIProfileService {
  private final ConnectionRef connectionRef;
  private ConcurrentMap<String, ObjectListItem> objectListItemMap;


  AIProfileService(ConnectionRef connectionRef) {
    assert connectionRef.get() != null : "No connection";
    this.connectionRef = connectionRef;
  }


  /**
   * Supplies the AI profile map of the current connection
   *
   * @return a map of profile by profile name. can be empty but not null
   * @throws ProfileManagementException
   */
  public CompletableFuture<List<Profile>> getProfiles() {
    if (Boolean.parseBoolean(System.getProperty("fake.services"))) {
      List<Profile> faked = new ArrayList<>();
      faked.add(Profile.builder().profileName("cohere").provider(
          ProviderType.COHERE).credentialName("foo").model("foo").build());
      return CompletableFuture.completedFuture(faked);
    }
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection dbnConnection =
            connectionRef.get().getConnection(SessionId.ORACLE_AI);
        List<Profile> profileList = connectionRef.get().getOracleAIInterface()
            .listProfiles(dbnConnection);
        return profileList;
      } catch (ProfileManagementException | SQLException e) {
        throw new CompletionException("Cannot get profile", e);
      }
    });
  }


  /**
   * Drops a profile on the remote server  asynchronously
   *
   * @param profileName the name of the profile to be deleted
   * @throws ProfileManagementException
   */
  public CompletableFuture<Void> deleteProfile(String profileName) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        connectionRef.get().getOracleAIInterface().dropProfile(connection, profileName);
      } catch (SQLException | ProfileManagementException e) {
        throw new CompletionException("Cannot delete profile", e);
      }
    });

  }

  /**
   * Creates a profile on the remote server  asynchronously
   *
   * @param profile the profile to be created
   * @throws ProfileManagementException
   */
  public CompletionStage<Void> createProfile(Profile profile) {
    return CompletableFuture.runAsync(() -> {
          try {
            DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
            connectionRef.get().getOracleAIInterface().createProfile(connection, profile);
          } catch (SQLException | ProfileManagementException e) {
            throw new CompletionException("Cannot create profile", e);
          }
        }
    );
  }

  /**
   * Updates a profile on the remote server  asynchronously
   *
   * @param updatedProfile the updated profile attributes
   * @throws ProfileManagementException
   */
  public CompletionStage<Void> updateProfile(Profile updatedProfile) {
    return CompletableFuture.runAsync(() -> {
          try {
            DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
            connectionRef.get().getOracleAIInterface().setProfileAttributes(connection, updatedProfile);
          } catch (SQLException | ProfileManagementException e) {
            throw new CompletionException("Cannot update profile", e);
          }
        }
    );
  }

  /**
   * Loads all schemas that are accessible for the current user asynchronously
   */

  // TODO : move this to another service
    public CompletableFuture<List<String>> loadSchemas() {
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        List<String> schemas = connectionRef.get().getOracleAIInterface().listSchemas(connection);

        return schemas;
      } catch (DatabaseOperationException | SQLException e) {
        throw new CompletionException("Cannot get schemas", e);
      }
    });
  }

  /**
   * Loads object list items of a certain profile asynchronously
   */
  public CompletableFuture<List<ObjectListItem>> loadObjectListItems(String profileName) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        List<ObjectListItem> objectListItemsList = connectionRef.get().getOracleAIInterface().listObjectListItems(connection, profileName);
        objectListItemMap = objectListItemsList.stream().collect(Collectors.toConcurrentMap((e) -> e.getName() + "_" + e.getOwner(), (e) -> e));
        return objectListItemsList;
      } catch (DatabaseOperationException | SQLException e) {
        throw new CompletionException("Cannot list object list items", e);
      }
    });
  }

  /**
   * Loads all object list item accessible to the current user asynchronously
   */
  public List<ObjectListItem> getObjectItems() {
    List<ObjectListItem> data = objectListItemMap.keySet().stream()
        .map(item -> objectListItemMap.get(item)
        )
        .collect(Collectors.toList());
    return data;
  }

  /**
   * Loads all object list items accessible to the user from a specific schema asynchronously
   */
  public List<ObjectListItem> getObjectItemsForSchema(String schema) {
    List<ObjectListItem> data = objectListItemMap.keySet().stream()
        .filter(item -> objectListItemMap.get(item).getOwner().equals(schema))
        .map(item -> objectListItemMap.get(item)
        )
        .collect(Collectors.toList());
    return data;
  }

}
