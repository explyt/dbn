package com.dbn.oracleAI.config;

import com.dbn.oracleAI.types.DatabaseObjectType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class is to define object list items ( tables & views ) for each profile instance we have, and specify whether they are selected
 */
@Getter
@AllArgsConstructor
/**
 * POJO class that represent an object in the database, basically table or view
 */
public class DBObjectItem {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DBObjectItem that = (DBObjectItem) o;
    //object name and owner ae case in-sensitive in Oracle DB
    return owner.equalsIgnoreCase(that.owner) && name.equalsIgnoreCase(that.name) &&
            type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, name, type);
  }

  @NotNull
  public String owner;
  @NotNull
  public String name;
  @NotNull
  public DatabaseObjectType type;

}
