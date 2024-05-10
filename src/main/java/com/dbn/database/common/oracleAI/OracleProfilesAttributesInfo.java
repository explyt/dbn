package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.types.ProviderType;
import lombok.Getter;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class OracleProfilesAttributesInfo implements CallableStatementOutput {

  private List<Profile> profileList = new ArrayList<>();

  public OracleProfilesAttributesInfo() {
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
    Map<String, Profile> profileBuildersMap = new HashMap<>();

    while (rs.next()) {
      String profileName = rs.getString("PROFILE_NAME");
      String status = rs.getString("STATUS");
      String description = rs.getString("DESCRIPTION");
      String attributeName = rs.getString("ATTRIBUTE_NAME");
      Clob attributeValue = rs.getClob("ATTRIBUTE_VALUE");
      Profile currProfile = profileBuildersMap.computeIfAbsent(profileName, k -> Profile.builder().profileName(profileName).build());
      currProfile.setEnabled(status.equals("ENABLED"));
      currProfile.setDescription(description);

      Object attributeObject = currProfile.clobToObject(attributeName, attributeValue);

      if ("object_list".equals(attributeName) && attributeObject instanceof List) {
        @SuppressWarnings("unchecked")
        List<ProfileDBObjectItem> objectList = (List<ProfileDBObjectItem>) attributeObject;
        currProfile.setObjectList(objectList);
      } else if (attributeObject instanceof String) {
        String attribute = (String) attributeObject;
        if (Objects.equals(attributeName, "temperature"))
          currProfile.setTemperature(Double.parseDouble(attribute));
        else if (Objects.equals(attributeName, "provider"))
          currProfile.setProvider(ProviderType.valueOf(attribute.toUpperCase()));
        else if (Objects.equals(attributeName, "credential_name")) currProfile.setCredentialName(attribute);
        else if (Objects.equals(attributeName, "model"))
          try {
            ProviderModel model = ProviderModel.getByName(attribute);
            currProfile.setModel(model);
          } catch (IllegalArgumentException e) {
            currProfile.setModel(currProfile.getProvider().getDefaultModel());
          }
      }
    }

    return new ArrayList<>(profileBuildersMap.values());

  }
}
