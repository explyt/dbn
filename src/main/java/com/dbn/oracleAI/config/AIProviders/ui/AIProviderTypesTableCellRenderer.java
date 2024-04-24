package com.dbn.oracleAI.config.AIProviders.ui;

import com.dbn.common.ui.table.DBNColoredTableCellRenderer;
import com.dbn.common.ui.table.DBNTable;
import com.dbn.common.util.Strings;
import com.intellij.ui.SimpleTextAttributes;

public class AIProviderTypesTableCellRenderer extends DBNColoredTableCellRenderer {
  @Override
  protected void customizeCellRenderer(DBNTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {

    String stringValue = (String) value;
    if (Strings.isNotEmpty(stringValue)) {
      append(stringValue, SimpleTextAttributes.REGULAR_ATTRIBUTES);
    }

  }
}
