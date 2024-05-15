package com.dbn.oracleAI.config.AIProviders.ui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

public class AIProviderTypesTableCellRenderer extends DefaultTableCellRenderer {
  private final int SECRET_COLUMN = 2;
  private final int VISIBLE_CHARS = 4;

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (column == SECRET_COLUMN && value != null) {
      String text = value.toString();
      if (text.length() > VISIBLE_CHARS) {
        text = text.substring(0, VISIBLE_CHARS) + "************";
      }
      value = text;
    }
    return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }
}
