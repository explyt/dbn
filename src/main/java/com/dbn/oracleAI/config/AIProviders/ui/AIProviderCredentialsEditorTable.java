package com.dbn.oracleAI.config.AIProviders.ui;

import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.table.DBNEditableTable;
import com.dbn.oracleAI.config.AIProviders.AIProviderCredentialBundle;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.DefaultCellEditor;
import javax.swing.JPasswordField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Table model for provider credentials information
 * The template credential stored locally.
 */
public class AIProviderCredentialsEditorTable extends DBNEditableTable<AIProviderCredentialTableModel> {

  AIProviderCredentialsEditorTable(DBNComponent parent, AIProviderCredentialBundle environmentTypes) {
    super(parent, createModel(parent.getProject(), environmentTypes), true);
    setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    setSelectionBackground(UIUtil.getTableBackground());
    setSelectionForeground(UIUtil.getTableForeground());
    setCellSelectionEnabled(true);
    setDefaultRenderer(String.class, new AIProviderCredentialTableCellRenderer());

    JPasswordField pwf = new JPasswordField();
    DefaultCellEditor editor = new DefaultCellEditor(pwf);
    getColumnModel().getColumn(AIProviderCredentialTableCellRenderer.SECRET_COLUMN).setCellEditor(editor);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        adjustColumnSizes();
      }
    });
  }

  @NotNull
  private static AIProviderCredentialTableModel createModel(Project project, AIProviderCredentialBundle environmentTypes) {
    return new AIProviderCredentialTableModel(project, environmentTypes);
  }

  private void adjustColumnSizes() {
    int tableWidth = getWidth();

    // Ensure that the table has columns before adjusting sizes
    if (getColumnModel().getColumnCount() > 2) {
      TableColumn column1 = getColumnModel().getColumn(0);
      TableColumn column2 = getColumnModel().getColumn(1);
      TableColumn column3 = getColumnModel().getColumn(2);

      // Set first two columns to 1/5 of the table each, and third column to 2/5 of the table
      int column1and2Width = tableWidth / 5;
      int column3Width = 2 * column1and2Width;

      column1.setPreferredWidth(column1and2Width);
      column2.setPreferredWidth(column1and2Width);
      column3.setPreferredWidth(column3Width);
    }
  }

  void setEnvironmentTypes(AIProviderCredentialBundle environmentTypes) {
    super.setModel(createModel(getProject(), environmentTypes));
    adjustColumnSizes();
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    return column < 4;
  }
}
