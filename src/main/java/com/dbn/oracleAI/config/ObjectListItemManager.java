package com.dbn.oracleAI.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is responsible for making sure that object list items instances are unique with the id being name+owner+profileName
 */
public class ObjectListItemManager {
  private static final Set<ObjectListItem> items = Collections.synchronizedSet(new HashSet<>());

  public static synchronized ObjectListItem getObjectListItem(String owner, String name) {
    for (ObjectListItem item : items) {
      if (name != null && owner != null) {
        if (item.getOwner().toUpperCase().equals(owner.toUpperCase()) && item.getName().toUpperCase().equals(name.toUpperCase())) {
          return item;
        }
      } else if (owner != null) {
        if (item.getOwner().toUpperCase().equals(owner.toUpperCase())) {
          return item;
        }
      }
    }
    ObjectListItem newItem = new ObjectListItem(owner.toUpperCase(), name.toUpperCase());
    items.add(newItem);
    return newItem;
  }
}
