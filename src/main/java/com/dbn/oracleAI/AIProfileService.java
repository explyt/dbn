package com.dbn.oracleAI;

import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;


public class AIProfileService {
  private final ConnectionRef connectionRef;

  private static final Logger LOGGER = Logger.getInstance(AIProfileService.class.getPackageName());


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
        LOGGER.debug("getting profiles");
        DBNConnection dbnConnection =
            connectionRef.get().getConnection(SessionId.ORACLE_AI);
        List<Profile> profileList = connectionRef.get().getOracleAIInterface()
            .listProfiles(dbnConnection);
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("fetched profiles:" + profileList);
        return profileList;
      } catch (ProfileManagementException | SQLException e) {
        LOGGER.error("error getting profiles", e);
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
        LOGGER.error("error deleting profile "+ profileName, e);
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
            LOGGER.error("error creating profile", e);
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
            LOGGER.error("error updating profiles", e);
            throw new CompletionException("Cannot update profile", e);
          }
        }
    );
  }

}
