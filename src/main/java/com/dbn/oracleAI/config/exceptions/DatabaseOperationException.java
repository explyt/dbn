package com.dbn.oracleAI.config.exceptions;

import lombok.Getter;

import java.sql.SQLException;

@Getter

/**
 * Custom exception class for database operation
 * TODO consider simplifying exception handling. DBN gets away with properly handling raw SQLException in all places (TBD).
 */
public class DatabaseOperationException extends SQLException {

  public DatabaseOperationException(String message, SQLException cause) {
    super(message, cause);
  }


  public DatabaseOperationException(String message) {
    super(message);
  }

}
