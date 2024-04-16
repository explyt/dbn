package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.dbn.oracleAI.types.ProviderType;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service to handle AI profiles
 */
public class AIProfileService {
  private final ConnectionHandler connectionHandler;

  AIProfileService(ConnectionHandler connectionHandler) {
    this.connectionHandler = connectionHandler;
  }


  /**
   * Supplies the AI profile map of the current connection
   * @return a map of profile by profile name. can be empty but not null
   */
  public CompletableFuture<Map<String, Profile>> getProfiles() {
    if (System.getProperty("fake.services") !=null) {
      Map<String, Profile> faked = new HashMap<String, Profile>();
      faked.put("cohere",Profile.builder().profileName("cohere").provider(
        ProviderType.COHERE).credentialName("foo").model("foo").build());
      return CompletableFuture.completedFuture(faked);
    }
    return CompletableFuture.supplyAsync(()-> {
      try {
        DBNConnection dbnConnection =
          connectionHandler.getConnection(SessionId.ORACLE_AI);
        List<Profile> profileList = connectionHandler.getOracleAIInterface()
                                                     .listProfilesDetailed(
                                                       dbnConnection);
        return profileList.stream()
                          .collect(Collectors.toMap(Profile::getProfileName,
                                                    Function.identity(),
                                                    (existing, replacement) -> existing));
      } catch (SQLException | DatabaseOperationException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    });
  }


  /**
   * Drops a profile on the remote server  asynchronously
   * @param profileName the name of the profile to be deleted
   * @throws ProfileManagementException
   */
  public CompletableFuture<Void> deleteProfile(String profileName) {
    return CompletableFuture.runAsync(() -> {
      try {
        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
          connectionHandler.getOracleAIInterface().dropProfile(connection, profileName);
      } catch (SQLException | ProfileManagementException e) {
        throw new CompletionException(e);
      }
    });

  }

  public CompletionStage<Void> addProfile(Profile editedProfile) {
    return CompletableFuture.completedFuture(null);
  }
}
