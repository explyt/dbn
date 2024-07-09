package com.dbn.common;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

public class SQLBundle {
  @NonNls private static final String COM_INTELLIJ_LANG_SQL_BUNDLE = "com.dbn.common.SQLBundle";
  private static final ResourceBundle ourBundle = ResourceBundle.getBundle(COM_INTELLIJ_LANG_SQL_BUNDLE);

  private  SQLBundle(){}

  public static String message(@PropertyKey(resourceBundle = "com.dbn.common.SQLBundle") String key, Object... params) {
    return AbstractBundle.message(ourBundle, key, params);
  }
}
