package com.dbn.database.common.oracleAI;


import com.dbn.database.common.statement.CallableStatementOutput;
import com.dbn.oracleAI.config.CredentialProvider;
import lombok.Getter;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Getter
public class OracleCredentialsInfo implements CallableStatementOutput {
  private List<CredentialProvider> credentials;


  @Override
  public void registerParameters(CallableStatement statement) throws SQLException {
    statement.registerOutParameter(1, Types.VARCHAR);
  }

  @Override
  public void read(CallableStatement statement) throws SQLException {
    credentials = Arrays.stream(statement.getString(1).split(" ")).map((credentialName)->CredentialProvider.builder().credentialName(credentialName).build()).collect(Collectors.toList());
  }
}
