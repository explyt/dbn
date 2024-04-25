package com.dbn.oracleAI.config;

import com.dbn.oracleAI.types.DataType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is responsible for making sure that object list items instances are unique with the id being name+owner+profileName
 */
public class ObjectListItemManager {
  private static final Set<ObjectListItem> items = Collections.synchronizedSet(new HashSet<>());

  public static synchronized ObjectListItem getObjectListItem(String owner, String name, DataType type) {
    for (ObjectListItem item : items) {
      if (name != null && owner != null) {
        if (item.getOwner().toUpperCase().equals(owner.toUpperCase()) && item.getName().toUpperCase().equals(name.toUpperCase())) {
          if (item.type == null) item.setType(type);
          return item;
        }
      } else if (owner != null) {
        if (item.getOwner().toUpperCase().equals(owner.toUpperCase())) {
          if (item.type == null) item.setType(type);
          return item;
        }
      }
    }
    ObjectListItem newItem = new ObjectListItem(owner.toUpperCase(), name.toUpperCase(), type);
    items.add(newItem);
    return newItem;
  }
}
