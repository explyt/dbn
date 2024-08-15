package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
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
public class AIProfileServiceImpl extends AIAssistantComponent implements AIProfileService {

  AIProfileServiceImpl(ConnectionHandler connection) {
      super(connection);
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
        DBNConnection connection = getAssistantConnection();
        List<Profile> profileList = getAssistantInterface().listProfiles(connection);

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
        DBNConnection connection = getAssistantConnection();
        getAssistantInterface().dropProfile(connection, profileName);
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
            DBNConnection connection = getAssistantConnection();
            getAssistantInterface().createProfile(connection, profile);
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
            DBNConnection connection = getAssistantConnection();
            getAssistantInterface().setProfileAttributes(connection, updatedProfile);
          } catch (SQLException | ProfileManagementException e) {
            log.warn("error updating profiles", e);
            throw new CompletionException("Cannot update profile", e);
          }
        }
    );
  }
}
