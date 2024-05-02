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
import java.lang.reflect.Type;
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


  @Override
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
          if (System.getProperty("fake.services.profile.dump") != null) {
              try {
                  FileWriter writer = new FileWriter(System.getProperty("fake.services.profile.dump"));
                  new Gson().toJson(profileList, writer);
                  writer.close();
              } catch (Exception e) {
                  // ignore this
                  if (LOGGER.isTraceEnabled())
                      LOGGER.trace("cannot dump profile " +e.getMessage());
              }
          }
        if (LOGGER.isDebugEnabled())
          LOGGER.debug("fetched profiles:" + profileList);

        return profileList;
      } catch (ProfileManagementException | SQLException e) {
        LOGGER.error("error getting profiles", e);
        throw new CompletionException("Cannot get profile", e);
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
