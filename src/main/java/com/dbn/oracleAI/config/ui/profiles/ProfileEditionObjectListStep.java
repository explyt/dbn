package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.DatabaseService;
import com.dbn.oracleAI.WizardStepChangeEvent;
import com.dbn.oracleAI.WizardStepEventListener;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.dbn.oracleAI.config.ui.SelectedObjectItemsVerifier;
import com.dbn.oracleAI.types.DatabaseObjectType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.eclipse.sisu.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Profile edition Object list step for edition wizard
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionObjectListStep extends AbstractProfileEditionStep {

  static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());
  private static final Logger LOGGER = Logger.getInstance(DatabaseService.class.getPackageName());

  private static int TABLES_COLUMN_HEADERS_NAME_IDX = 0;
  private static int TABLES_COLUMN_HEADERS_OWNER_IDX = 1;
  private static String[] TABLES_COLUMN_HEADERS = {
          messages.getString("profile.mgmt.obj_table.header.name"),
          messages.getString("profile.mgmt.obj_table.header.owner")
  };

  private JPanel profileEditionObjectListMainPane;
  private JCheckBox selectAllCheckBox;
  private JTextField patternFilter;
  private JTable profileObjectListTable;
  private JTable databaseObjectsTable;
  private JLabel selectedTablesLabel;
  private JComboBox schemaComboBox;
  private JCheckBox withViewsButton;
  private JProgressBar activityProgress;
  private final AIProfileService profileSvc;
  private final DatabaseService databaseSvc;
  private final Project project;
  ProfileObjectListTableModel profileObjListTableModel = new ProfileObjectListTableModel();
  DatabaseObjectListTableModel dbObjListTableModel = new DatabaseObjectListTableModel();

  Map<String,List<DBObjectItem>> schemaObjectListCache = new HashMap<>();

  public ProfileEditionObjectListStep(Project project, Profile profile) {
    super();
    this.profileSvc = project.getService(DatabaseOracleAIManager.class).getProfileService();
    this.project = project;
    this.databaseSvc = project.getService(DatabaseOracleAIManager.class).getDatabaseService();

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
        populateDatabaseObjectTable(schemaComboBox.getSelectedItem().toString());
      }
    });

    withViewsButton.addItemListener((
        e -> {
          populateDatabaseObjectTable(schemaComboBox.getSelectedItem().toString());
        }
    ));
    initializeTable(profile);
  }

  private void initializeTable(@Nullable Profile profile) {
    LOGGER.debug("initializing tables");
    profileObjectListTable.setModel(profileObjListTableModel);
    addValidationListener(profileObjectListTable);
    databaseObjectsTable.setModel(dbObjListTableModel);
    databaseObjectsTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int row = databaseObjectsTable.rowAtPoint(e.getPoint());
          if (row >= 0) {
            DBObjectItem item = dbObjListTableModel.getItemAt(row);
            if (profileObjListTableModel.contains(item)) {
              return;
            }
            profileObjListTableModel.addItem(
                    new ProfileDBObjectItem(item.getOwner(),item.getName()));
            dbObjListTableModel.hideItem(item);
          }
        }
      }
    });
    profileObjectListTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int row = profileObjectListTable.rowAtPoint(e.getPoint());
          if (row >= 0) {
            ProfileDBObjectItem item = profileObjListTableModel.getItemAt(row);
            dbObjListTableModel.unhideItem(item.getOwner(),item.getName());
            profileObjListTableModel.removeItem(item);
          }
        }
      }
    });

    loadSchemas();
    // now that we have schemas loaded we can add the listener
    schemaComboBox.addActionListener((e) -> {
      LOGGER.debug("action listener on  schemaComboBox fired");
      populateDatabaseObjectTable(schemaComboBox.getSelectedItem().toString());
    });

    populateProfileObjectLitTable(profile);
    ((TableRowSorter)databaseObjectsTable.getRowSorter()).setRowFilter(new RowFilter<DatabaseObjectListTableModel, Integer>() {

      @Override
      public boolean include(Entry<? extends DatabaseObjectListTableModel, ? extends Integer> entry) {
        // first : is it already selected ? if yes should be hidden
        if (entry.getModel().alreadySelectedItems.contains(entry.getModel().getItemAt((Integer)entry.getIdentifier()))) {
          return false;
        }

        // second : does it match selected schema ?
        if (!entry.getStringValue(TABLES_COLUMN_HEADERS_OWNER_IDX).equalsIgnoreCase(schemaComboBox.getSelectedItem().toString())) {
          return false;
        }

        // third : does it match the current patten if any
        if ((patternFilter.getText().length() > 0) && !entry.getStringValue(TABLES_COLUMN_HEADERS_NAME_IDX).matches(patternFilter.getText())) {
          return false;
        }
        return true;
      }
    });
    databaseObjectsTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
      private final JTextField editor = new JTextField();

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        editor.setText(value.toString());
        editor.setBorder(null);
        editor.setEditable(false);
        editor.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        editor.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        // Check if the current row item is in the selectedTableModel
        DBObjectItem currentItem = dbObjListTableModel.getItemAt(row);
        if (currentItem.getType() == DatabaseObjectType.VIEW) {
          editor.setFont(editor.getFont().deriveFont(Font.ITALIC));
          editor.setForeground(Color.LIGHT_GRAY);
        } else {
          editor.setFont(editor.getFont().deriveFont(Font.PLAIN));
          editor.setForeground(Color.BLACK);
        }
        return editor;
      }
    });
    profileObjectListTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
      private final JTextField editor = new JTextField();

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        editor.setText((value != null) ? value.toString() : "*");
        editor.setBorder(null);
        editor.setEditable(false);
        editor.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        editor.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        // Check if the current row item is in the selectedTableModel
        ProfileDBObjectItem currentItem = profileObjListTableModel.getItemAt(row);
        if (locateTypeFor(currentItem) == DatabaseObjectType.VIEW) {
          editor.setFont(editor.getFont().deriveFont(Font.ITALIC));
          editor.setForeground(Color.LIGHT_GRAY);
        } else {
          editor.setFont(editor.getFont().deriveFont(Font.PLAIN));
          editor.setForeground(Color.BLACK);
        }
        return editor;
      }
    });
  }

  private void startActivityNotifier() {
    activityProgress.setIndeterminate(true);
    activityProgress.setVisible(true);
  }

  /**
   * Stops the spining wheel
   */
  private void stopActivityNotifier() {
    activityProgress.setIndeterminate(false);
    activityProgress.setVisible(false);
  }

  /**
   * Looks into DB object model for a matching type
   * @param item object item in a profile
   * @return the type of that item or null if it a wildcard object (i.e name == null)
   */
  private DatabaseObjectType locateTypeFor(ProfileDBObjectItem item) {
    if (item.getName() == null || item.getName().length() == 0) {
      return null;
    }
    // no way to not find something
    return schemaObjectListCache.get(item.getOwner())
            .stream().filter(item::isEquivalentTo).findFirst().get().getType();
  }

  private void loadSchemas() {
    startActivityNotifier();
    databaseSvc.getSchemaNames().thenAccept(schemaList -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        //schemaComboBox.unhideItem("All Schemas");
        for (String schema : schemaList) {
          schemaComboBox.addItem(schema);
        }
        stopActivityNotifier();
      });
    }).exceptionally(e -> {
      Messages.showErrorDialog(project,"Cannot load schemas",
              "Cannot load DB schemas: "+e.getCause().getMessage());
      return null;
    });
  }

  private void adjustColumnSizes(JTable table) {
    int tableWidth = table.getWidth();

    // Ensure that the table has columns before adjusting sizes
    if (table.getColumnModel().getColumnCount() > 1) {
      TableColumn column1 = table.getColumnModel().getColumn(0);
      TableColumn column2 = table.getColumnModel().getColumn(1);

      // Set each column to half of the table width
      int columnWidth = tableWidth / 3;

      column1.setPreferredWidth(2 * columnWidth);
      column2.setPreferredWidth(columnWidth);
    }
  }


  private void populateProfileObjectLitTable(Profile profile) {
    if (profile != null) {
      // if null: we are in the middle of a profile creation
      profileObjListTableModel.updateItems(profile.getObjectList());
    }
    //adjustColumnSizes(profileObjectListTable);
//    profileObjectListTable.addComponentListener(new ComponentAdapter() {
//      @Override
//      public void componentResized(ComponentEvent e) {
//        adjustColumnSizes(profileObjectListTable);
//      }
//    });
  }

  private void populateDatabaseObjectTable(String schema) {
    List<DBObjectItem> objects = schemaObjectListCache.get(schema);
    if (objects == null) {
      startActivityNotifier();
      databaseObjectsTable.setEnabled(false);
      databaseSvc.getObjectItemsForSchema(schema).thenAccept(objs->{
        ApplicationManager.getApplication().invokeLater(() -> {
          schemaObjectListCache.put(schema,objs);
          dbObjListTableModel.updateItems(objs);
         // adjustColumnSizes(databaseObjectsTable);
          stopActivityNotifier();
          databaseObjectsTable.setEnabled(true);
        });
      }).exceptionally(e -> {
        Messages.showErrorDialog(project,"Cannot fetch objects",
                "Cannot fetching database object list: "+e.getCause().getMessage());
        return null;
      });
    } else {
      dbObjListTableModel.updateItems(objects);
    }

//    databaseObjectsTable.addComponentListener(new ComponentAdapter() {
//      @Override
//      public void componentResized(ComponentEvent e) {
//        adjustColumnSizes(databaseObjectsTable);
//      }
//    });

  }


  @Override
  public JPanel getPanel() {
    return profileEditionObjectListMainPane;
  }

  @Override
  public void setAttributesOn(Profile p) {
    p.setObjectList(profileObjListTableModel.data);
  }

  public class ProfileObjectListTableModel extends AbstractTableModel {
    private List<ProfileDBObjectItem> data;

    private static final int NAME_COLUMN_IDX = 0;
    private static final int OWNER_COLUMN_IDX = 1;
    private String[] columnNames = TABLES_COLUMN_HEADERS;

    public ProfileObjectListTableModel() {
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
      ProfileDBObjectItem item = data.get(rowIndex);
      switch (columnIndex) {
        case NAME_COLUMN_IDX:
          return item.getName();
        case OWNER_COLUMN_IDX:
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
    public void addItem(ProfileDBObjectItem item) {
      data.add(item);
      fireTableRowsInserted(data.size() - 1, data.size() - 1);
      dbObjListTableModel.fireTableDataChanged(); // Refresh other table to update bold styling
      fireTableDataChanged();
    }

    public void removeItem(ProfileDBObjectItem item) {
      int index = data.indexOf(item);
      if (index >= 0) {
        data.remove(index);
//        fireTableRowsDeleted(index, index);
//        fireTableDataChanged();

      }
    }

    public void updateItems(List<ProfileDBObjectItem> items) {
      data.clear();
      data.addAll(items);
      fireTableRowsInserted(0, data.size() - 1);
    }

    // Method to get data
    public List<ProfileDBObjectItem> getData() {
      return data;
    }

    // Method to check if the model contains a specific item
    public boolean contains(DBObjectItem item) {
      // check that we have already this.
      // that means we have something that match owner/name
      // or owner/*
      return data.contains(item);
    }


    public ProfileDBObjectItem getItemAt(int rowIndex) {
      return data.get(rowIndex);
    }
  }


  public class DatabaseObjectListTableModel extends AbstractTableModel {

    //items that do not match the object name pattern
    private List<DBObjectItem> allItems;

    //items that are already selected, moved to profile object list
    List<DBObjectItem> alreadySelectedItems;

    private String[] columnNames = TABLES_COLUMN_HEADERS;

    public DatabaseObjectListTableModel() {
      this.allItems = new ArrayList<>();
      this.alreadySelectedItems = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
      return allItems.size();
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      DBObjectItem item = allItems.get(rowIndex);
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
      return String.class;
    }


    public void hideItem(DBObjectItem item) {
      int index = allItems.indexOf(item);
      if (index >= 0) {
        alreadySelectedItems.add(item);
        allItems.remove(index);
        fireTableRowsDeleted(index, index);
        fireTableDataChanged();
      }
    }

    /**
     * move away all items from the alreadySelectedItems list
     * @param item the item to be revealed
     */
    /**
     * Moves away all items from the alreadySelectedItems list that
     * match criterias
     * @param itemOwner item owner
     * @param itemName  item name, can be null meaning "all"
     */
    private void unhideItem(String itemOwner, String itemName) {
      // first deal with wild card
      // in that case, unhide all from the owner
      if (itemName == null) {
        alreadySelectedItems.removeIf(o->o.getOwner().equalsIgnoreCase(itemOwner));
      } else {
        alreadySelectedItems.removeIf(o-> (
                o.getOwner().equalsIgnoreCase(itemOwner) &&
                o.getName().equalsIgnoreCase(itemName)));
      }
      fireTableRowsInserted(0, getRowCount() - 1);
      fireTableDataChanged();

    }

    public DBObjectItem getItemAt(int rowIndex) {
      return allItems.get(rowIndex);
    }

    public void updateItems(List<DBObjectItem> objs) {
      assert objs != null:"cannot be null";
      LOGGER.debug("DatabaseObjectListTableModel.updateItems with "+ objs.size());
      allItems.clear();
      allItems.addAll(objs);
      this.fireTableDataChanged();
    }
  }


  private void addValidationListener(JTable table) {
    table.setInputVerifier(new SelectedObjectItemsVerifier());
    table.getModel().addTableModelListener(e -> {
      fireWizardStepChangeEvent();
    });
  }

  private void fireWizardStepChangeEvent() {
    for (WizardStepEventListener listener : this.listeners) {
      listener.onStepChange(new WizardStepChangeEvent(this));
    }
  }

  @Override
  public boolean isInputsValid() {
    // TODO : add more
    return profileObjectListTable.getInputVerifier().verify(profileObjectListTable);
  }


}
