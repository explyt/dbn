package com.dbn.oracleAI.config.AIProviders.ui;

import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.table.DBNEditableTable;
import com.dbn.oracleAI.config.AIProviders.AIProviderTypeBundle;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

public class AIProviderEditorTable extends DBNEditableTable<AIProviderTypesTableModel> {

  AIProviderEditorTable(DBNComponent parent, AIProviderTypeBundle environmentTypes) {
    super(parent, createModel(parent.getProject(), environmentTypes), true);
    setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    setSelectionBackground(UIUtil.getTableBackground());
    setSelectionForeground(UIUtil.getTableForeground());
    setCellSelectionEnabled(true);
    setDefaultRenderer(String.class, new AIProviderTypesTableCellRenderer());

    setFixedWidth(columnModel.getColumn(2), 100);

  }

  @NotNull
  private static AIProviderTypesTableModel createModel(Project project, AIProviderTypeBundle environmentTypes) {
    return new AIProviderTypesTableModel(project, environmentTypes);
  }

  void setEnvironmentTypes(AIProviderTypeBundle environmentTypes) {
    super.setModel(createModel(getProject(), environmentTypes));
    setFixedWidth(columnModel.getColumn(2), 100);
    setFixedWidth(columnModel.getColumn(3), 100);
    setFixedWidth(columnModel.getColumn(4), 60);
  }

  private void setFixedWidth(TableColumn tableColumn, int width) {
    tableColumn.setMaxWidth(width);
    tableColumn.setMinWidth(width);
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return column < 4;
  }
}
