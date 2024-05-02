package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.dbn.oracleAI.types.ProviderType;
import lombok.Getter;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class OracleProfilesAttributesInfo implements CallableStatementOutput {

  private List<Profile> profileList;
  private Map<String, Profile> profileMap;

  public OracleProfilesAttributesInfo(Map<String, Profile> profileMap) {
    this.profileMap = profileMap;
  }

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    try {
      ResultSet rs = statement.executeQuery();
      profileList = buildProfilesFromResultSet(rs);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Since the result set has each attribute in a separate row, it was read accordingly
   */
  private List<Profile> buildProfilesFromResultSet(ResultSet rs) throws SQLException, IOException {

    while (rs.next()) {
      String profileName = rs.getString("PROFILE_NAME");
      String attributeName = rs.getString("ATTRIBUTE_NAME");
      java.sql.Clob clobData = rs.getClob("ATTRIBUTE_VALUE");
      Profile currProfile = profileMap.get(profileName);
      Object attributeObject = currProfile.clobToObject(attributeName, clobData);

      if ("object_list".equals(attributeName) && attributeObject instanceof List) {
        @SuppressWarnings("unchecked")
        List<ProfileDBObjectItem> objectList = (List<ProfileDBObjectItem>) attributeObject;
        currProfile.setObjectList(objectList);
      } else if (attributeObject instanceof String) {
        String attributeValue = (String) attributeObject;
        if (Objects.equals(attributeName, "temperature"))
          currProfile.setTemperature(Double.parseDouble(attributeValue));
        else if (Objects.equals(attributeName, "provider"))
          currProfile.setProvider(ProviderType.valueOf(attributeValue.toUpperCase()));
        else if (Objects.equals(attributeName, "credential_name")) currProfile.setCredentialName(attributeValue);
      }
    }

    return new ArrayList<>(profileMap.values());

  }
}
