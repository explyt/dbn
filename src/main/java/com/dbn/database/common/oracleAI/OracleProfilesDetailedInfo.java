package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.ObjectListItem;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ProviderType;
import lombok.Getter;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Getter
public class OracleProfilesDetailedInfo implements CallableStatementOutput{

    private List<Profile> profileList;

    @Override
    public void registerParameters(CallableStatement statement) throws SQLException {
    }

    @Override
    public void read(CallableStatement statement) throws SQLException {
      try{
        ResultSet rs = statement.executeQuery();
        profileList = buildProfilesFromResultSet(rs);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

  /**
   * Since the result set has each attribute in a separate row, it was read accordingly
   */
  public static List<Profile> buildProfilesFromResultSet(ResultSet rs) throws SQLException, IOException {
    Map<String, Profile> profileBuildersMap = new HashMap<>();

    while (rs.next()) {
      String profileName = rs.getString("PROFILE_NAME");
      String attributeName = rs.getString("ATTRIBUTE_NAME");
      java.sql.Clob clobData = rs.getClob("ATTRIBUTE_VALUE");

      Profile profile = profileBuildersMap.computeIfAbsent(profileName, k -> Profile.builder().profileName(profileName).build());
      Object attributeObject = profile.clobToObject(attributeName, clobData);

      if ("object_list".equals(attributeName) && attributeObject instanceof List) {
        @SuppressWarnings("unchecked")
        List<ObjectListItem> objectList = (List<ObjectListItem>) attributeObject;
        profile.setObjectList(objectList);
      } else if (attributeObject instanceof String) {
        String attributeValue = (String) attributeObject;
        if(Objects.equals(attributeName, "temperature")) profile.setTemperature(Double.parseDouble(attributeValue));
        else if(Objects.equals(attributeName, "provider")) profile.setProvider(ProviderType.valueOf(attributeValue.toUpperCase()));
        else if(Objects.equals(attributeName, "credential_name")) profile.setCredentialName(attributeValue);
      }
    }

    List<Profile> profiles = new ArrayList<>(profileBuildersMap.values());

    return profiles;

  }
}
