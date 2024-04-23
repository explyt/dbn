package com.dbn.oracleAI.config.exceptions;

import java.sql.SQLException;

public class DatabaseConnectionException extends DatabaseOperationException {
  public DatabaseConnectionException(String message) {
    super(message);
  }

  public DatabaseConnectionException(String message, SQLException cause) {
    super(message, cause);
  }
}
