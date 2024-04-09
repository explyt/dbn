package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.Profile;
import lombok.Getter;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents detailed information about Oracle AI credentials, including associated profiles.
 * This class is designed to process the output of a callable statement that retrieves
 * credential information from an Oracle database.
 */
@Getter
public class OracleCredentialsDetailedInfo implements CallableStatementOutput {

  private List<Profile> profileList;
  private List<CredentialProvider> credentialsProviders;

  /**
   * Constructs an instance of OracleCredentialsDetailedInfo with a pre-defined list of profiles.
   *
   * @param profileList The list of profiles associated with the credentials.
   */
  public OracleCredentialsDetailedInfo(List<Profile> profileList) {
    this.profileList = profileList;
  }

  /**
   * Registers parameters with the provided CallableStatement.
   * This method is meant to be implemented for statements that require parameter registration,
   * but it is not utilized in this class.
   *
   * @param statement The CallableStatement with which parameters are to be registered.
   * @throws SQLException If there is a database access error or other errors.
   */
  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
    // Implementation not required for this class's context
  }

  /**
   * Reads and processes the output of a CallableStatement that executes a query
   * to retrieve credential information from the database. It builds a list of CredentialProvider
   * objects containing credentials and associated profiles.
   *
   * @param statement The CallableStatement from which to read the query output.
   * @throws SQLException If there is a database access error, the SQL statement is not a query,
   *                      or other SQL errors occur.
   */
  @Override
  public void read(CallableStatement statement) throws SQLException {
    ResultSet rs = statement.executeQuery();
    credentialsProviders = buildCredentialProviders(rs);
  }

  /**
   * Private helper method to construct a list of CredentialProvider objects from a ResultSet.
   * The method maps credential names to CredentialProvider objects and associates profiles
   * with their corresponding credentials.
   *
   * @param rs The ResultSet containing credential information from the database.
   * @return A list of CredentialProvider objects constructed from the ResultSet data.
   * @throws SQLException If an error occurs while accessing the ResultSet.
   */
  private List<CredentialProvider> buildCredentialProviders(ResultSet rs) throws SQLException {
    Map<String, CredentialProvider> credentialProviderBuildersMap = new HashMap<>();

    // Iterate over ResultSet to populate credentialProviderBuildersMap
    while (rs.next()) {
      String credentialName = rs.getString("CREDENTIAL_NAME");
      String username = rs.getString("USERNAME");
      credentialProviderBuildersMap.computeIfAbsent(credentialName, k -> CredentialProvider.builder().credentialName(credentialName).username(username).build());
    }

    // Associate profiles with corresponding CredentialProvider objects
    for (Profile profile : this.profileList) {
      credentialProviderBuildersMap.get(profile.getCredentialName()).getProfiles().add(profile);
    }

    // Convert map values to a list and return
    return new ArrayList<>(credentialProviderBuildersMap.values());
  }
}
