package com.dbn.database.common.oracleAI;


import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.Profile;
import lombok.Getter;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class OracleProfilesInfo implements CallableStatementOutput {

  private List<Profile> profiles;

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
    statement.registerOutParameter(1, Types.VARCHAR);
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    String result = statement.getString(1);
    if (result == null) {
      profiles = List.of();
    } else {
      profiles = Arrays.stream(result.split(" "))
                       .map((s) -> Profile.builder().profileName(s).build())
                       .collect(Collectors.toList());
    }
    }
}
