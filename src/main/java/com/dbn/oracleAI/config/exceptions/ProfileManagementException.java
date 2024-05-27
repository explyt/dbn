package com.dbn.oracleAI.config.exceptions;

import java.sql.SQLException;

/**
 * Custom exception class for profile management
 */
public class ProfileManagementException extends DatabaseOperationException {
  public ProfileManagementException(String message) {
    super(message);
  }

  public ProfileManagementException(String message, SQLException cause) {
    super(message, cause);
  }
}

