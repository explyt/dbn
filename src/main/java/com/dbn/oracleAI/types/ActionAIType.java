package com.dbn.oracleAI.types;

import lombok.Getter;

@Getter
/**
 * Select AI action enum class
 * @see https://docs.oracle.com/en/cloud/paas/autonomous-database/serverless/adbsb/sql-generation-ai-autonomous.html#ADBSB-GUID-B3E0EE68-3B4C-4002-9B45-BBE258A2F15A
 */
public enum ActionAIType {
  SHOWSQL("showsql"),
  EXPLAINSQL("explainsql"),
  EXECUTESQL("executesql"),
  NARRATE("narrate"),
  CHAT("chat");

  private final String action;

  ActionAIType(String action) {
    this.action = action;
  }

  public static ActionAIType getByAction(String action) {
    for (ActionAIType type : values()) {
      if (type.getAction().equals(action)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid action: " + action);
  }
}
