package com.dbn.oracleAI.config.exceptions;

import lombok.Getter;

import java.sql.SQLException;

@Getter

/**
 * Custom exception class for database operation
 */
public class DatabaseOperationException extends Exception {

  public DatabaseOperationException(String message, SQLException cause) {
    super(message, cause);
  }


  public DatabaseOperationException(String message) {
    super(message);
  }

}
