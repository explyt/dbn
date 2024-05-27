package com.dbn.database.common.oracleAI;

import com.dbn.database.common.statement.CallableStatementOutput;
import lombok.Getter;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

@Getter
/**
 * CallableStatementOutput to get list of views
 */
public class OracleViewsList implements CallableStatementOutput{

  private String[] views;

  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
    statement.registerOutParameter(1, Types.VARCHAR);
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    views = statement.getString(1).split(" ");
  }
}
