package com.dbn.database.oracle;

import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.common.DatabaseInterfaceBase;
import com.dbn.database.common.oracleAI.*;
import com.dbn.database.interfaces.DatabaseInterfaces;
import com.dbn.database.interfaces.DatabaseOracleAIInterface;
import com.dbn.oracleAI.config.ObjectListItem;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ActionAIType;

import java.sql.SQLException;
import java.util.*;

public class OracleAIInterface extends DatabaseInterfaceBase implements DatabaseOracleAIInterface {

  public OracleAIInterface(DatabaseInterfaces provider) {
    super("oracle_ai_interface.xml", provider);
  }

  @Override
  public void createCredential(DBNConnection connection, CredentialProvider credentialAttributes) throws CredentialManagementException {
    try {
      executeCall(connection, null, "create-credential", credentialAttributes.getCredentialName(), credentialAttributes.toAttributeMap());
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to create credential: " + credentialAttributes.getCredentialName(), e);
    }
  }

  @Override
  public void dropCredential(DBNConnection connection, String credentialName) throws CredentialManagementException {
    try {
      executeCall(connection, null, "drop-credential", credentialName);
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to drop credential: " + credentialName, e);
    }
  }

  @Override
  public void setCredentialAttribute(DBNConnection connection, String credentialName, String attributeName, String attributeValue) throws CredentialManagementException {
    try {
      executeCall(connection, null, "update-credential", credentialName, attributeName, attributeValue);
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to set credential attribute: " + credentialName, e);
    }
  }

  @Override
  public void createProfile(DBNConnection connection, Profile profileAttributes) throws ProfileManagementException {
    try {
      executeCall(connection, null, "create-profile", profileAttributes.getProfileName(), profileAttributes.toAttributeMap());
    } catch (SQLException e) {
      throw new ProfileManagementException("Failed to create profile: " + profileAttributes.getProfileName() + "\n" + e.getMessage(), e);
    }
  }

  @Override
  public void dropProfile(DBNConnection connection, String profileName) throws ProfileManagementException {
    try {
      executeCall(connection, null, "drop-profile", profileName);
    } catch (SQLException e) {
      throw new ProfileManagementException("Failed to drop profile: " + profileName, e);
    }
  }

  @Override
  public void setProfileAttributes(DBNConnection connection, Profile profileAttribute) throws ProfileManagementException {
    try {
      executeCall(connection, null, "update-profile", profileAttribute.toAttributeMap());
    } catch (SQLException e) {
      throw new ProfileManagementException("Failed to set profile attribute: " + profileAttribute.getProfileName(), e);
    }
  }

  @Override
  public OracleQueryOutput executeQuery(DBNConnection connection, ActionAIType action, String profile, String text) throws QueryExecutionException {
    try {
      return executeCall(connection, new OracleQueryOutput(), "ai-query", profile, action, text, "");
    } catch (SQLException e) {
      throw new QueryExecutionException("Failed to query\n", e);
    }
  }


  @Override
  public OracleTablesList listTables(DBNConnection connection) throws DatabaseOperationException {
    try {
      return executeCall(connection, new OracleTablesList(), "list-tables");
    } catch (SQLException e) {
      throw new DatabaseOperationException("Failed to list tables", e);
    }
  }

  @Override
  public OracleViewsList listViews(DBNConnection connection) throws DatabaseOperationException {
    try {
      return executeCall(connection, new OracleViewsList(), "list-views");
    } catch (SQLException e) {
      throw new DatabaseOperationException("Failed to list views", e);
    }
  }

  @Override
  public List<CredentialProvider> listCredentials(DBNConnection connection) throws CredentialManagementException {
    try {
      return executeCall(connection, new OracleCredentialsInfo(), "list-credentials").getCredentials();
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to list credential names", e);
    }
  }

  @Override
  public List<Profile> listProfiles(DBNConnection connection) throws DatabaseOperationException {
    try {
      return executeCall(connection, new OracleProfilesInfo(), "list-profiles").getProfiles();
    } catch (SQLException e) {
      throw new DatabaseOperationException("Failed to list profiles", e);
    }
  }

  @Override
  public List<CredentialProvider> listCredentialsDetailed(DBNConnection connection) throws CredentialManagementException {
    try{
      List<Profile> profileList = listProfilesDetailed(connection);
      List<CredentialProvider> credentialProviders = executeCall(connection, new OracleCredentialsDetailedInfo(profileList), "list-credentials-detailed", profileList).getCredentialsProviders();
      return credentialProviders;
    } catch (SQLException e) {
      throw new CredentialManagementException("Failed to list credentials", e);
    } catch (DatabaseOperationException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  public List<Profile> listProfilesDetailed(DBNConnection connection) throws ProfileManagementException {
    try {
      List<Profile> profileList = executeCall(connection, new OracleProfilesDetailedInfo(), "list-profiles-detailed").getProfileList();
      return profileList;
    } catch (SQLException e) {
      throw new ProfileManagementException(e.getMessage(), e);
    }
  }

  @Override
  public List<String> listSchemas(DBNConnection connection) throws DatabaseOperationException {
    try {
      List<String> schemaList = executeCall(connection, new SchemasInfo(), "list-schemas").getSchemaList();
      return schemaList;
    } catch (SQLException e) {
      throw new DatabaseOperationException(e.getMessage(), e);
    }
  }

  @Override
  public List<ObjectListItem> listObjectListItems(DBNConnection connection, String profileName) throws DatabaseOperationException {
    try {
      List<ObjectListItem> objectListItemsList = executeCall(connection, new ObjectListItemInfo(profileName), "list-tables").getObjectListItemsList();
      return objectListItemsList;
    } catch (SQLException e) {
      throw new DatabaseOperationException(e.getMessage(), e);
    }
  }


}

