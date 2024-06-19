package com.dbn.oracleAI.types;

import lombok.Getter;

@Getter
public enum DatabaseObjectType {
  TABLE("TABLE_NAME"),
  VIEW("VIEW_NAME"),
  MATERIALIZED_VIEW("MATERIALIZED_VIEW_NAME");

  // That column's name of remote DB views
  private final String columnName;

  DatabaseObjectType(String columnName) {
    this.columnName = columnName;
  }
}



