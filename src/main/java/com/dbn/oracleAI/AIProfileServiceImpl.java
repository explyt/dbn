package com.dbn.oracleAI;

import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.google.gson.Gson;
import com.intellij.openapi.diagnostic.Logger;

import java.io.FileWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;


public class AIProfileServiceImpl implements AIProfileService {
  private final ConnectionRef connectionRef;

  private static final Logger LOGGER = Logger.getInstance(AIProfileServiceImpl.class.getPackageName());


  AIProfileServiceImpl(ConnectionRef connectionRef) {
    assert connectionRef.get() != null : "No connection";
    this.connectionRef = connectionRef;
  }

  private void dumpThem(List<Profile> profileList, String path) {
      if (path != null ) {
          try {
              FileWriter writer = new FileWriter(path);
              new Gson().toJson(profileList, writer);
              writer.close();
          } catch (Exception e) {
              // ignore this
              if (LOGGER.isTraceEnabled())
                  LOGGER.trace("cannot dump profile " +e.getMessage());
          }
      }
  }

  @Override
  public CompletableFuture<List<Profile>> getProfiles()  {
    return CompletableFuture.supplyAsync(() -> {
      try {
        LOGGER.debug("getting profiles");
        DBNConnection dbnConnection =
            connectionRef.get().getConnection(SessionId.ORACLE_AI);
        List<Profile> profileList = connectionRef.get().getOracleAIInterface()
            .listProfiles(dbnConnection);

        dumpThem(profileList, System.getProperty("fake.services.profiles.dump") );

        if (LOGGER.isDebugEnabled())
          LOGGER.debug("fetched profiles:" + profileList);
          return profileList;
      } catch (ProfileManagementException | SQLException e) {
        LOGGER.error("error getting profiles", e);
        throw new CompletionException("Cannot get profiles", e);
      }
    });
  }


  @Override
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

  @Override
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

  @Override
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
