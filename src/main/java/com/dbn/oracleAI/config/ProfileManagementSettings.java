//package com.dbn.oracleAI.config;
//
//import com.dbn.common.util.Messages;
//import com.dbn.connection.ConnectionHandler;
//import com.dbn.connection.ConnectionId;
//import com.dbn.connection.SessionId;
//import com.dbn.connection.config.AIProfileSettings;
//import com.dbn.connection.jdbc.DBNConnection;
//import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
//import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
//import com.dbn.oracleAI.config.ui.ProfileCreationWindow;
//import com.intellij.openapi.application.ApplicationManager;
//
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//public class ProfileManagementSettings {
//  private final ConnectionHandler connectionHandler;
//
//  public ProfileManagementSettings(ConnectionHandler connectionHandler) {
//    this.connectionHandler = connectionHandler;
//  }
//
//  public Map<String, Profile> loadProfiles() {
//    try {
//      DBNConnection dbnConnection = connectionHandler.getConnection(SessionId.ORACLE_AI);
//      List<Profile> profileList = connectionHandler.getOracleAIInterface().listProfilesDetailed(dbnConnection);
//      return profileList.stream().collect(Collectors.toMap(Profile::getProfileName, Function.identity(), (existing, replacement) -> existing));
//    } catch (SQLException | RuntimeException e) {
//      Messages.showErrorDialog(connectionHandler.getProject(), "Error", "Failed to load profiles: " + e.getMessage());
//      return null;
//    } catch (DatabaseOperationException e) {
//      throw new RuntimeException(e);
//    }
//  }
//
//  public boolean deleteProfile(String profileName) {
//    try {
//      DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
//      connectionHandler.getOracleAIInterface().dropProfile(connection, profileName);
//      return true;
//    } catch (SQLException | ProfileManagementException e) {
//      Messages.showErrorDialog(connectionHandler.getProject(), "Error", "Failed to delete profile: " + e.getMessage());
//      return false;
//    }
//  }
//
//  public void openProfileCreationSettings(ProfileManagementSettings profileManager){
//    ProfileCreationWindow settingsWindow = new ProfileCreationWindow(connectionHandler, profileManager);
//    settingsWindow.display();
//  }
//
//  public CompletableFuture<List<CredentialProvider>> loadCredentials() {
//    return CompletableFuture.supplyAsync(()-> {
//      try {
//        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
//        List<CredentialProvider> credentials = connectionHandler.getOracleAIInterface().listCredentials(connection);
//        return credentials;
//      } catch (DatabaseOperationException e) {
//        throw new RuntimeException(e);
//      } catch (SQLException e) {
//        System.out.println(e);
//        return null;
//      }
//    });
//  }
//
//  public CompletableFuture<List<String>> loadSchemas(){
//    return CompletableFuture.supplyAsync(()-> {
//      try {
//        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
//        List<String> schemas = connectionHandler.getOracleAIInterface().listSchemas(connection);
//        return schemas;
//      } catch (DatabaseOperationException | SQLException e) {
//        throw new RuntimeException(e);
//      }
//    });
//  }
//
//  public CompletableFuture<List<ObjectListItem>> loadObjectListItems(){
//    return CompletableFuture.supplyAsync(()-> {
//      try {
//        DBNConnection connection = connectionHandler.getConnection(SessionId.ORACLE_AI);
//        List<ObjectListItem> objectListItemsList = connectionHandler.getOracleAIInterface().listObjectListItems(connection);
//        return objectListItemsList;
//      } catch (DatabaseOperationException | SQLException e) {
//        throw new RuntimeException(e);
//      }
//    });
//  }
//
//
//}
