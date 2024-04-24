package com.dbn.oracleAI.config;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * This class is to define object list items ( tables & views ) for each profile instance we have, and specify whether they are selected
 */
@Setter
@Getter
@AllArgsConstructor
public class ObjectListItem {
  @NotNull
  public String owner;
  public String name;

}