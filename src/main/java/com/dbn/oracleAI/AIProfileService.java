package com.dbn.oracleAI;

import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
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
  public CompletableFuture<List<Profile>> getProfiles()  {

    if (Boolean.parseBoolean(System.getProperty("fake.services.profile"))) {
       Type PROFILE_TYPE = new TypeToken<List<Profile>>() {}.getType();
        List<Profile> profiles = null;
        try {
            profiles = new Gson().fromJson(new FileReader("/var/tmp/profiles.json"),PROFILE_TYPE);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return CompletableFuture.completedFuture(profiles);
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
        FileWriter writer = new FileWriter("/var/tmp/profiles.json");
        new Gson().toJson(profileList,writer);
        writer.close();
        return profileList;
      } catch (ProfileManagementException | SQLException e) {
        LOGGER.error("error getting profiles", e);
        throw new CompletionException("Cannot get profile", e);
      } catch (IOException e) {
          throw new RuntimeException(e);
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
