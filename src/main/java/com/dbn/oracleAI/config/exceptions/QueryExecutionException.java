package com.dbn.oracleAI.config.exceptions;

import java.sql.SQLException;

public class QueryExecutionException extends DatabaseOperationException {
  public QueryExecutionException(String message) {
    super(message);
  }

  public QueryExecutionException(String message, int codeError, SQLException cause){
    super(message, cause);
  }

  public QueryExecutionException(String message, SQLException cause) {
    super(message, cause);
  }
}

