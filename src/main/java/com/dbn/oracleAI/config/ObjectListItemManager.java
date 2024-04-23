package com.dbn.oracleAI.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is responsible for making sure that object list items instances are unique with the id being name+owner+profileName
 */
public class ObjectListItemManager {
  private static final Set<ObjectListItem> items = Collections.synchronizedSet(new HashSet<>());

  public static synchronized ObjectListItem getObjectListItem(String owner, String name, String profileName) {
    for (ObjectListItem item : items) {
      if (item.getOwner().toUpperCase().equals(owner.toUpperCase()) && item.getName().toUpperCase().equals(name.toUpperCase())) {
        return item;
      }
    }
    ObjectListItem newItem = new ObjectListItem(owner, name);
    items.add(newItem);
    return newItem;
  }
}
