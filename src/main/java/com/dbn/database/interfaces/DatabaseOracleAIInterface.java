package com.dbn.database.interfaces;

import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.common.oracleAI.OracleQueryOutput;
import com.dbn.database.common.oracleAI.OracleTablesList;
import com.dbn.database.common.oracleAI.OracleViewsList;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.types.ActionAIType;

import java.util.List;

/**
 * Defines the interface for managing Oracle AI profiles and credentials in a database.
 * This includes creating, updating, and deleting credentials and profiles,
 * executing AI-related queries, and listing database tables, views, credentials, and profiles.
 */
public interface DatabaseOracleAIInterface extends DatabaseInterface {

  /**
   * Creates a new credential for accessing external services or databases.
   *
   * @param connection           The database connection object representing a session with the database.
   * @param credentialAttributes The attributes for the new credential to be created.
   * @throws CredentialManagementException If there is an error in creating the credential.
   */
  void createCredential(DBNConnection connection, Credential credentialAttributes) throws CredentialManagementException;

  /**
   * Removes an existing credential from the database.
   *
   * @param connection     The database connection object.
   * @param credentialName The name of the credential to remove.
   * @throws CredentialManagementException If there is an error in removing the credential.
   */
  void dropCredential(DBNConnection connection, String credentialName) throws CredentialManagementException;

  /**
   * Updates an attribute of an existing credential in the database.
   *
   * @param connection The database connection object.
   * @throws CredentialManagementException If there is an error in updating the credential attribute.
   */
  void setCredentialAttribute(DBNConnection connection, Credential credential) throws CredentialManagementException;

  /**
   * Creates a new AI profile for executing AI-related operations in the database.
   *
   * @param connection        The database connection object.
   * @param profileAttributes The attributes for the new AI profile to be created.
   * @throws ProfileManagementException If there is an error in creating the AI profile.
   */
  void createProfile(DBNConnection connection, Profile profileAttributes) throws ProfileManagementException;

  /**
   * Updates an attribute of an existing AI profile in the database.
   *
   * @param connection The database connection object.
   * @param profile    The AI profile whose attribute is to be updated. The update will be for each attribute separately
   * @throws ProfileManagementException If there is an error in updating the AI profile attribute.
   */
  void setProfileAttributes(DBNConnection connection, Profile profile) throws ProfileManagementException;

  /**
   * Deletes an existing AI profile from the database.
   *
   * @param connection  The database connection object.
   * @param profileName The name of the AI profile to be deleted.
   * @throws ProfileManagementException If there is an error in deleting the AI profile.
   */
  void dropProfile(DBNConnection connection, String profileName) throws ProfileManagementException;

  /**
   * Executes an AI-related query using a specified action and text on a specific profile.
   *
   * @param connection The database connection object.
   * @param action     The AI action to perform, such as translate or analyze.
   * @param profile    The name of the AI profile to use for the query execution.
   * @param text       The text or query to process using the AI action.
   * @return The result of the AI query execution.
   * @throws QueryExecutionException If there is an error in executing the AI query.
   */
  OracleQueryOutput executeQuery(DBNConnection connection, ActionAIType action, String profile, String text, String model) throws QueryExecutionException;

  /**
   * Lists all tables available in the current database schema.
   *
   * @param connection The database connection object.
   * @return A list of table names in the database schema.
   * @throws DatabaseOperationException If there is an error in listing the tables.
   */
  OracleTablesList listTables(DBNConnection connection) throws DatabaseOperationException;

  /**
   * Lists all views available in the current database schema.
   *
   * @param connection The database connection object.
   * @return A list of view names in the database schema.
   * @throws DatabaseOperationException If there is an error in listing the views.
   */
  OracleViewsList listViews(DBNConnection connection) throws DatabaseOperationException;

  /**
   * Lists all credentials stored in the database.
   *
   * @param connection The database connection object.
   * @return A list containing information about each stored credential.
   * @throws DatabaseOperationException If there is an error in retrieving the list of credentials.
   */
  List<Credential> listCredentials(DBNConnection connection) throws CredentialManagementException;


  /**
   * Provides a detailed list of all AI profiles stored in the database.
   *
   * @param connection The database connection object.
   * @return A detailed list containing information about each AI profile.
   * @throws DatabaseOperationException If there is an error in retrieving the detailed list of AI profiles.
   */
  List<Profile> listProfiles(DBNConnection connection) throws ProfileManagementException;

  /**
   * Lists all schemas available in the current database connection.
   *
   * @param connection The database connection object.
   * @return A list of schema names available to the current connection.
   * @throws DatabaseOperationException If there is an error in listing the schemas.
   */
  List<String> listSchemas(DBNConnection connection) throws DatabaseOperationException;

  /**
   * Lists all objects items available in the current database connection.
   * This can include tables, views.
   *
   * @param connection The database connection object.
   * @param schemaName the name of the schema object belong to.
   * @return A list of database object.
   * @throws DatabaseOperationException If there is an error in retrieving the list of database objects.
   */
  List<DBObjectItem> listObjectListItems(DBNConnection connection, String schemaName) throws DatabaseOperationException;

}
