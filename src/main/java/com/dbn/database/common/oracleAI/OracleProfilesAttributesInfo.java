package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.dbn.oracleAI.types.ProviderType;
import com.google.gson.JsonParseException;
import com.intellij.openapi.diagnostic.Logger;
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

  private static final Logger LOGGER = Logger.getInstance("com.dbn.oracleAI");

  private List<Profile> profileList = new ArrayList<>();

  public OracleProfilesAttributesInfo() {
  }

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
      ResultSet rs = statement.executeQuery();
      profileList = buildProfilesFromResultSet(rs);
  }

  /**
   * Since the result set has each attribute in a separate row, it was read accordingly
   */
  private List<Profile> buildProfilesFromResultSet(ResultSet rs) throws SQLException {
    Map<String, Profile> profileBuildersMap = new HashMap<>();
    List <String> faultyProfilesNames = new ArrayList<>();
    while (rs.next()) {
      String profileName=null;
      try {
        profileName = rs.getString("PROFILE_NAME");
        String status = rs.getString("STATUS");
        String description = rs.getString("DESCRIPTION");
        String attributeName = rs.getString("ATTRIBUTE_NAME");
        Clob attributeValue = rs.getClob("ATTRIBUTE_VALUE");
        String finalProfileName = profileName;
        Profile currProfile = profileBuildersMap.computeIfAbsent(profileName, k -> Profile.builder().profileName(finalProfileName).build());
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
        }
      } catch (IOException e) {
        // This one is a real one.
        throw new SQLException(e);
      } catch (JsonParseException ee) {
        // this is surely because of DB tables sending us bad values.
        LOGGER.error("malformed profile [" + profileName + "], dropping it");
        faultyProfilesNames.add(profileName);
      }
    }
    // now remove all faulty ones
    profileBuildersMap.keySet().removeAll(faultyProfilesNames);
    return new ArrayList<>(profileBuildersMap.values());

  }
}
