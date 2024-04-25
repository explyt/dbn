package com.dbn.oracleAI.types;

import lombok.Getter;

@Getter
public enum DataType {
  TABLE("TABLE_NAME"),
  VIEW("VIEW_NAME");

  private final String action;

  DataType(String action) {
    this.action = action;
  }
}



