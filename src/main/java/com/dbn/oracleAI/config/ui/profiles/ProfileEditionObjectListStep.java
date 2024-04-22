package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.ObjectListItem;
import com.dbn.oracleAI.config.Profile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.eclipse.sisu.Nullable;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Profile edition Object list step for edition wizard
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionObjectListStep extends AbstractProfileEditionStep {
  private JPanel profileEditionObjectListMainPane;
  private JCheckBox useAllCheckBox;
  private JTextField patternFilter;
  private JTable selectedTable;
  private JTable selectingTable;
  private JLabel selectedTablesLabel;
  private JComboBox schemaComboBox;
  private final AIProfileService profileSvc;
  private final Project project;
  ObjectListSelectedTableModel selectedTableModel = new ObjectListSelectedTableModel();
  ObjectListSelectingTableModel selectingTableModel = new ObjectListSelectingTableModel();
  private String pattern = "";


  public ProfileEditionObjectListStep(Project project, Profile profile) {
    super();
    this.profileSvc = project.getService(DatabaseOracleAIManager.class).getProfileService();
    this.project = project;
    patternFilter.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        filter();
      }

      public void removeUpdate(DocumentEvent e) {
        filter();
      }

      public void insertUpdate(DocumentEvent e) {
        filter();
      }

      public void filter() {
        pattern = patternFilter.getText();
        populateSelectingTable(schemaComboBox.getSelectedItem().toString());
      }
    });

    useAllCheckBox.addItemListener(
        e -> {
          if (useAllCheckBox.isSelected()) selectingTableModel.selectAllFiltered();
        }
    );
    initializeTable(profile);
  }

  private void initializeTable(@Nullable Profile profile) {
    selectedTable.setModel(selectedTableModel);
    selectingTable.setModel(selectingTableModel);
    loadSchemas();
    loadTables(profile);
    selectingTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
      private final JTextField editor = new JTextField();

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        editor.setText((value != null) ? value.toString() : "");
        editor.setBorder(null);
        editor.setEditable(false);
        editor.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        editor.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return editor;
      }
    });
    selectingTable.setSelectionModel(new ProfileEditionObjectListStep.NullSelectionModel());
  }

  private void loadSchemas() {
    notifyLoading("Loading tables/views");
    profileSvc.loadSchemas().thenAccept(schemaList -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        stopLoading();
        schemaComboBox.addItem("All Schemas");
        for (String schema : schemaList) {
          schemaComboBox.addItem(schema);
        }
      });
    }).exceptionally(e -> {
      Messages.showErrorDialog(project, e.getCause().getMessage());
      return null;
    });
  }

  private void loadTables(@Nullable Profile profile) {
    profileSvc.loadObjectListItems(profile != null ? profile.getProfileName() : "").thenAccept(objectListItems -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        populateSelectingTable(schemaComboBox.getSelectedItem().toString());
        populateSelectedTable(profile);
        schemaComboBox.addActionListener((e) -> {
          populateSelectingTable(schemaComboBox.getSelectedItem().toString());
        });
      });

    }).exceptionally(e -> {
      Messages.showErrorDialog(project, e.getCause().getMessage());
      return null;
    });
  }

  private void populateSelectedTable(Profile profile) {
    if (profile != null) {
      selectedTableModel.updateItems(profile.getObjectList());
    }
  }

  private void populateSelectingTable(String schema) {
    if (schema.equals("All Schemas")) selectingTableModel.updateItems(profileSvc.getObjectItems());
    else selectingTableModel.updateItems(profileSvc.getObjectItemsForSchema(schema));
    selectingTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JCheckBox()) {
      @Override
      public boolean stopCellEditing() {
        int row = selectingTable.getEditingRow();
        boolean isSelected = (Boolean) getCellEditorValue();
        ObjectListItem item = selectingTableModel.getItemAt(row);

        if (isSelected) {
          if (!selectedTableModel.contains(item)) {
            selectedTableModel.addItem(item);
          }
        } else {
          selectedTableModel.removeItem(item);
        }
        return super.stopCellEditing();
      }
    });
  }

  private static class NullSelectionModel extends DefaultListSelectionModel {
    @Override
    public void setSelectionInterval(int index0, int index1) {
      super.setSelectionInterval(-1, -1);
    }
  }

  @Override
  public JPanel getPanel() {
    return profileEditionObjectListMainPane;
  }

  @Override
  public void setAttributesOn(Profile p) {
    p.setObjectList(selectedTableModel.data);
  }

  public class ObjectListSelectedTableModel extends AbstractTableModel {
    private List<ObjectListItem> data;
    private String[] columnNames = {"Table/View Name", "Owner"};

    public ObjectListSelectedTableModel() {
      this.data = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
      return data.size();
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      ObjectListItem item = data.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return item.getName();
        case 1:
          return item.getOwner();
        default:
          return null;
      }
    }

    @Override
    public String getColumnName(int column) {
      return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == 1) {
        return String.class;
      }
      return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    // Methods to manipulate the data
    public void addItem(ObjectListItem item) {
      data.add(item);
      fireTableRowsInserted(data.size() - 1, data.size() - 1);
      selectingTableModel.fireTableDataChanged();
    }

    public void removeItem(ObjectListItem item) {
      int index = data.indexOf(item);
      if (index >= 0) {
        data.remove(index);
        fireTableRowsDeleted(index, index);
        selectingTableModel.fireTableDataChanged(); // Refresh other table
      }
    }

    public void updateItems(List<ObjectListItem> items) {
      data.clear();
      for (ObjectListItem item : items) {
        data.add(item);
      }
      fireTableRowsInserted(0, data.size() - 1);
    }

    // Method to get data
    public List<ObjectListItem> getData() {
      return data;
    }

    // Method to check if the model contains a specific item
    public boolean contains(ObjectListItem item) {
      return data.contains(item);
    }

    public ObjectListItem getItemAt(int rowIndex) {
      return data.get(rowIndex);
    }
  }


  public class ObjectListSelectingTableModel extends AbstractTableModel {

    private List<ObjectListItem> filteredData;
    private String[] columnNames = {"Table/View Name", "Selected"};

    @Override
    public boolean isCellEditable(int row, int column) {
      return column == 1;
    }

    public ObjectListSelectingTableModel() {
      this.filteredData = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
      return filteredData.size();
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      ObjectListItem item = filteredData.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return item.getName();
        case 1:
          return selectedTableModel.getData().contains(item);
        default:
          return null;
      }
    }

    @Override
    public String getColumnName(int column) {
      return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      if (columnIndex == 1) {
        return Boolean.class;
      }
      return String.class;
    }

    // Methods to manipulate the data
    public void updateItems(List<ObjectListItem> items) {
      filteredData = filterItems(items, pattern);
      fireTableRowsInserted(filteredData.size() - 1, filteredData.size() - 1);
    }

    public void selectAllFiltered() {
      for (int i = 0; i < filteredData.size(); i++) {
        ObjectListItem item = filteredData.get(i);
        selectedTableModel.addItem(item);
      }
      fireTableRowsUpdated(0, filteredData.size() - 1);

    }

    private List<ObjectListItem> filterItems(List<ObjectListItem> items, String pattern) {
      List<ObjectListItem> filteredItems = items.stream().filter(item -> item.getName().toLowerCase().contains(pattern.toLowerCase())).collect(Collectors.toList());
      return filteredItems;
    }

    public void removeItem(ObjectListItem item) {
      int index = filteredData.indexOf(item);
      if (index >= 0) {
        filteredData.remove(index);
        fireTableRowsDeleted(index, index);
      }
    }

    public ObjectListItem getItemAt(int rowIndex) {
      return filteredData.get(rowIndex);
    }
  }

  ;

}
