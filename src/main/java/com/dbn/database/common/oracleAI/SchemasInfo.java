package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import lombok.Getter;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class SchemasInfo implements CallableStatementOutput {
  private List<String> schemaList;

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    schemaList = new ArrayList<>();

    ResultSet rs = statement.executeQuery();
    while (rs.next()) {
      String owner = rs.getString("OWNER");
      schemaList.add(owner);
    }
  }
}
