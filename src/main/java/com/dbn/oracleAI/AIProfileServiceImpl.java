package com.dbn.oracleAI;

import com.dbn.connection.ConnectionRef;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@Slf4j
public class AIProfileServiceImpl implements AIProfileService {
  private final ConnectionRef connectionRef;

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
              if (log.isTraceEnabled())
                  log.trace("cannot dump profile " +e.getMessage());
          }
      }
  }

    @Override
    public CompletableFuture<Profile> get(String uuid) {
        assert false:"implement this !";
        return null;
    }

    @Override
  public CompletableFuture<List<Profile>> list()  {
    return CompletableFuture.supplyAsync(() -> {
      try {
        log.debug("getting profiles");
        DBNConnection dbnConnection =
            connectionRef.get().getConnection(SessionId.ORACLE_AI);
        List<Profile> profileList = connectionRef.get().getOracleAIInterface()
            .listProfiles(dbnConnection);

        dumpThem(profileList, System.getProperty("fake.services.profiles.dump") );

        if (log.isDebugEnabled())
          log.debug("fetched profiles:" + profileList);
          return profileList;
      } catch (ProfileManagementException | SQLException e) {
        log.warn("error getting profiles", e);
        throw new CompletionException("Cannot get profiles", e);
      }
    });
  }


  @Override
  public CompletableFuture<Void> delete(String profileName) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
        connectionRef.get().getOracleAIInterface().dropProfile(connection, profileName);
      } catch (SQLException | ProfileManagementException e) {
        log.warn("error deleting profile "+ profileName, e);
        throw new CompletionException("Cannot delete profile", e);
      }
    });

  }

  @Override
  public CompletionStage<Void> create(Profile profile) {
    return CompletableFuture.runAsync(() -> {
          try {
            DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
            connectionRef.get().getOracleAIInterface().createProfile(connection, profile);
          } catch (SQLException | ProfileManagementException e) {
            log.warn("error creating profile", e);
            throw new CompletionException("Cannot create profile", e);
          }
        }
    );
  }

  @Override
  public CompletionStage<Void> update(Profile updatedProfile) {
    return CompletableFuture.runAsync(() -> {
          try {
            DBNConnection connection = connectionRef.get().getConnection(SessionId.ORACLE_AI);
            connectionRef.get().getOracleAIInterface().setProfileAttributes(connection, updatedProfile);
          } catch (SQLException | ProfileManagementException e) {
            log.warn("error updating profiles", e);
            throw new CompletionException("Cannot update profile", e);
          }
        }
    );
  }

}
