package com.dbn.oracleAI.config.providers.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class AIProviderCredentialTableCellRenderer extends DefaultTableCellRenderer {
  public static final int SECRET_COLUMN = 2;
  public static final int VISIBLE_CHARS = 4;

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (column == SECRET_COLUMN && value != null) {
      String text = value.toString();
      if (text.length() > VISIBLE_CHARS) {
        text = text.substring(0, VISIBLE_CHARS) + "************";
      }
      value = "************";
    }
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }
}
