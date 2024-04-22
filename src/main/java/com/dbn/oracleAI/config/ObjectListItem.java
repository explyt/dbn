package com.dbn.oracleAI.config;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

/**
 * This class is to define object list items ( tables & views ) for each profile instance we have, and specify whether they are selected
 */
@Setter
@Getter
public class ObjectListItem {
  @Expose
  public String owner;
  @Expose
  public String name;

  public ObjectListItem(String owner, String name) {
    this.owner = owner;
    this.name = name;
  }
}