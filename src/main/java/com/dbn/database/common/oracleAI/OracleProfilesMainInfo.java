package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.Profile;
import lombok.Getter;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class OracleProfilesMainInfo implements CallableStatementOutput {

  private Map<String, Profile> profileMap;

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    try {
      ResultSet rs = statement.executeQuery();
      profileMap = buildProfilesFromResultSet(rs);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Since the result set has each attribute in a separate row, it was read accordingly
   */
  private Map<String, Profile> buildProfilesFromResultSet(ResultSet rs) throws SQLException, IOException {
    Map<String, Profile> profileBuildersMap = new HashMap<>();

    while (rs.next()) {
      String profileName = rs.getString("PROFILE_NAME");
      String status = rs.getString("STATUS");
      String description = rs.getString("DESCRIPTION");
      Profile profile = profileBuildersMap.computeIfAbsent(profileName, k -> Profile.builder().profileName(profileName).build());
      profile.setEnabled(status.equals("ENABLED"));
      profile.setDescription(description);
    }

    return profileBuildersMap;

  }
}
