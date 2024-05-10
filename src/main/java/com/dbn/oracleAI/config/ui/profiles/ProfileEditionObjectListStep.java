package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.DatabaseService;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.dbn.oracleAI.config.ui.SelectedObjectItemsVerifier;
import com.dbn.oracleAI.types.DatabaseObjectType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Profile edition Object list step for edition wizard
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionObjectListStep extends WizardStep<ProfileEditionGeneralModel> {

  static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());
  private static final Logger LOGGER = Logger.getInstance(DatabaseService.class.getPackageName());

  private static final int TABLES_COLUMN_HEADERS_NAME_IDX = 0;
  private static final int TABLES_COLUMN_HEADERS_OWNER_IDX = 1;
  private static final String[] PROFILE_OBJ_TABLES_COLUMN_HEADERS = {
      messages.getString("profile.mgmt.obj_table.header.name"),
      messages.getString("profile.mgmt.obj_table.header.owner")
  };
  private static final String[] DB_OBJ_TABLES_COLUMN_HEADERS = {
      messages.getString("profile.mgmt.obj_table.header.name")
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
  private final Profile profile;
  private final boolean isUpdate;
  ProfileObjectListTableModel profileObjListTableModel = new ProfileObjectListTableModel();

  //At start initialize it with empty one
  DatabaseObjectListTableModel currentDbObjListTableModel = new DatabaseObjectListTableModel();

  Map<String, DatabaseObjectListTableModel> schemaObjectListCache = new HashMap<>();

  public ProfileEditionObjectListStep(Project project, Profile profile, boolean isUpdate) {
    super("Object List Settings");
    this.profileSvc = project.getService(DatabaseOracleAIManager.class).getProfileService();
    this.project = project;
    this.profile = profile;
    this.isUpdate = isUpdate;
    this.databaseSvc = project.getService(DatabaseOracleAIManager.class).getDatabaseService();

    addListeners();
    initializeTables();
  }

  private void addListeners() {
//    patternFilter.getDocument().addDocumentListener(new DocumentListener() {
//      public void changedUpdate(DocumentEvent e) {
//        filter();
//      }
//
//      public void removeUpdate(DocumentEvent e) {
//        filter();
//      }
//
//      public void insertUpdate(DocumentEvent e) {
//        filter();
//      }

//      public void filter() {
//        RowFilter<TableModel, Integer> rf;
//        try {
//          rf = RowFilter.regexFilter("(?i)" + Pattern.quote(patternFilter.getText()));
//        } catch (java.util.regex.PatternSyntaxException e) {
//          return; // If current expression doesn't parse, don't update.
//        }
//        SwingUtilities.invokeLater(() -> {
//          sorter.setRowFilter(rf);
//        });
//      }
//    });

    withViewsButton.addItemListener((
        e -> {
          if (((JCheckBox) e.getSource()).isSelected()) {
            currentDbObjListTableModel.unhideItemByType(DatabaseObjectType.VIEW);
          } else {
            currentDbObjListTableModel.hideItemByType(DatabaseObjectType.VIEW);
          }

        }
    ));

    selectAllCheckBox.addItemListener((
        e -> {
          if (selectAllCheckBox.isSelected()) {
            List<ProfileDBObjectItem> newItems = new ArrayList<>();
            currentDbObjListTableModel.allItems.forEach(item -> {
              newItems.add(new ProfileDBObjectItem(
                  item.getOwner(),
                  item.getName()));
            });
            profileObjListTableModel.addItems(newItems);
            currentDbObjListTableModel.hideItemByNames(newItems.stream().map(item -> item.getName()).collect(Collectors.toList()));
          }
        }
    ));

    this.profileObjectListTable.setTransferHandler(new TransferHandler() {
      public boolean canImport(TransferHandler.TransferSupport info) {
        // Check for String flavor
        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
          return false;
        }
        return true;
      }

      public boolean importData(TransferHandler.TransferSupport info) {
        System.out.println();
        return true;
      }
    });

  }

  private void initializeTables() {
    LOGGER.debug("initializing tables", null, null, null);
    profileObjectListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    profileObjectListTable.setModel(profileObjListTableModel);
    profileObjectListTable.setInputVerifier(new SelectedObjectItemsVerifier());
    databaseObjectsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    databaseObjectsTable.setModel(currentDbObjListTableModel);
    databaseObjectsTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          JTable table = ((JTable) e.getSource());
          int[] selectedRows = table.getSelectedRows();
          if (selectedRows != null) {
            // hide all and add to profile obj list
            List<String> namesTobeHidden = new ArrayList<>(selectedRows.length);
            List<ProfileDBObjectItem> newItems = new ArrayList<>(selectedRows.length);

            for (int i = 0; i < selectedRows.length; i++) {
              DBObjectItem item = currentDbObjListTableModel.getItemAt(selectedRows[i]);
              newItems.add(new ProfileDBObjectItem(
                  item.getOwner(),
                  item.getName()));
              namesTobeHidden.add(item.getName());

            }
            profileObjListTableModel.addItems(newItems);
            currentDbObjListTableModel.hideItemByNames(namesTobeHidden);
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
            // we may be viewing datbase object from another schema
            // locate it first. no way that cache not already populated
            DatabaseObjectListTableModel model = schemaObjectListCache.get(item.getOwner());
            assert model != null : "trying to unhide items form model not in the cache";
            // TODO : verify that this do nto fire event when this is not the current model
            //        of the table
            model.unhideItem(item.getOwner(), item.getName());
            profileObjListTableModel.removeItem(item);
          }
        }
      }
    });


    // now that we have schemas loaded we can add the listener
    schemaComboBox.addActionListener((e) -> {
      LOGGER.debug("action listener on  schemaComboBox fired");
      populateDatabaseObjectTable(schemaComboBox.getSelectedItem().toString());
    });
    loadSchemas();

    if (isUpdate) {
      SwingUtilities.invokeLater(() -> {
        profileObjListTableModel.updateItems(profile.getObjectList());
      });
    }

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
        DBObjectItem currentItem = currentDbObjListTableModel.getItemAt(row);
        if (currentItem.getType() == DatabaseObjectType.VIEW) {
          editor.setFont(editor.getFont().deriveFont(Font.ITALIC));
          editor.setForeground(JBColor.LIGHT_GRAY);
        } else {
          editor.setFont(editor.getFont().deriveFont(Font.PLAIN));
          editor.setForeground(JBColor.BLACK);
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
   *
   * @param item object item in a profile
   * @return the type of that item or null if it a wildcard object (i.e name == null)
   */
  private DatabaseObjectType locateTypeFor(ProfileDBObjectItem item) {
    if (item.getName() == null || item.getName().length() == 0) {
      return null;
    }

    DatabaseObjectListTableModel model = schemaObjectListCache.get(item.getOwner());
    Optional<DBObjectItem> oitem = model.allItems.stream().filter(item::isEquivalentTo).findFirst();
    if (oitem.isEmpty()) {
      // look for hidden ones then
      oitem = model.parkedItems.stream().filter(item::isEquivalentTo).findFirst();
    }
    // at this point, no way to no have something
    return oitem.get().getType();
  }

  private void loadSchemas() {
    startActivityNotifier();
    databaseSvc.getSchemaNames().thenAccept(schemaList -> {
      SwingUtilities.invokeLater(() -> {
        for (String schema : schemaList) {
          LOGGER.debug("Adding new schema to dropbox: " + schema);
          schemaComboBox.addItem(schema);
        }
        stopActivityNotifier();
      });
    }).exceptionally(e -> {
      Messages.showErrorDialog(project, "Cannot load schemas",
          "Cannot load DB schemas: " + e.getCause().getMessage());
      return null;
    });
  }

  private void populateDatabaseObjectTable(String schema) {
    DatabaseObjectListTableModel model = schemaObjectListCache.get(schema);
    if (model == null) {
      startActivityNotifier();
      databaseObjectsTable.setEnabled(false);
      databaseSvc.getObjectItemsForSchema(schema).thenAccept(objs -> {
        SwingUtilities.invokeLater(() -> {
          DatabaseObjectListTableModel newModel = new DatabaseObjectListTableModel(objs, !withViewsButton.isSelected());
          schemaObjectListCache.put(schema, newModel);
          databaseObjectsTable.setModel(newModel);
          currentDbObjListTableModel = newModel;
          stopActivityNotifier();
          databaseObjectsTable.setEnabled(true);
        });
      }).exceptionally(e -> {
        Messages.showErrorDialog(project, "Cannot fetch objects",
            "Cannot fetching database object list: " + e.getCause().getMessage());
        return null;
      });
    } else {
      currentDbObjListTableModel = model;
      databaseObjectsTable.setModel(model);
    }
  }


  @Override
  public @org.jetbrains.annotations.Nullable String getHelpId() {
    return null;
  }

  @Override
  public JComponent prepare(WizardNavigationState wizardNavigationState) {
    return profileEditionObjectListMainPane;
  }

  @Override
  public @org.jetbrains.annotations.Nullable JComponent getPreferredFocusedComponent() {
    return null;
  }

  public class ProfileObjectListTableModel extends AbstractTableModel {
    private List<ProfileDBObjectItem> data;

    private static final int NAME_COLUMN_IDX = 0;
    private static final int OWNER_COLUMN_IDX = 1;
    private String[] columnNames = PROFILE_OBJ_TABLES_COLUMN_HEADERS;

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
    public void addItems(List<ProfileDBObjectItem> items) {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("ProfileObjectListTableModel.addItems: " + items);
      int curRow = data.size();
      data.addAll(items);
      LOGGER.debug(
          "ProfileObjectListTableModel.addItems triggered  fireTableRowsInserted on (" +
              curRow + "/" + curRow + items.size() + ")");
      fireTableRowsInserted(curRow, curRow + items.size());
    }

    public void removeItem(ProfileDBObjectItem item) {
      LOGGER.debug("ProfileObjectListTableModel.removeItem: " + item);
      int index = data.indexOf(item);
      if (index >= 0) {
        data.remove(index);
        LOGGER.debug(
            "ProfileObjectListTableModel.removeItem triggered  fireTableRowsDeleted on (" +
                index + "/" + index + ")");
        fireTableRowsDeleted(index, index);
      }
    }

    public void updateItems(List<ProfileDBObjectItem> items) {
      data.clear();
      data.addAll(items);
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("ProfileObjectListTableModel.updateItems: " + items);
      fireTableDataChanged();
    }

    // Method to get data
    public List<ProfileDBObjectItem> getData() {
      return data;
    }


    public ProfileDBObjectItem getItemAt(int rowIndex) {
      return data.get(rowIndex);
    }
  }


  public static class DatabaseObjectListTableModel extends AbstractTableModel {

    //items that do not match the object name pattern
    private List<DBObjectItem> allItems;

    //items that are already selected, moved to profile object list
    //These items will be parked out so to not be displayed
    List<DBObjectItem> parkedItems;

    private String[] columnNames = DB_OBJ_TABLES_COLUMN_HEADERS;

    public DatabaseObjectListTableModel() {
      this.allItems = new ArrayList<>();
      this.parkedItems = new ArrayList<>();
    }

    public DatabaseObjectListTableModel(List<DBObjectItem> objs, boolean hideViewsByDefault) {
      this.parkedItems = new ArrayList<>();
      if (hideViewsByDefault) {
        this.allItems = new ArrayList<>();
        for (DBObjectItem obj : objs) {
          if (obj.getType().equals(DatabaseObjectType.TABLE)) {
            this.allItems.add(obj);
          } else {
            // this is a view
            this.parkedItems.add(obj);
          }
        }

      } else {
        // include all
        this.allItems = objs;
      }
    }

    @Override
    public int getRowCount() {
      return this.allItems.size();
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      DBObjectItem item = allItems.get(rowIndex);
      switch (columnIndex) {
        case TABLES_COLUMN_HEADERS_NAME_IDX:
          return item.getName();
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

    public void hideItemByType(DatabaseObjectType databaseObjectType) {
      LOGGER.debug("DatabaseObjectListTableModel.hideItemByType: " + databaseObjectType);
      List<DBObjectItem> matches = new ArrayList<>();
      allItems.stream().filter(item -> item.getType().equals(databaseObjectType)).allMatch(
          match -> matches.add(match)
      );
      if (matches.size() > 0) {
        parkedItems.addAll(matches);
        allItems.removeAll(matches);
        LOGGER.debug("DatabaseObjectListTableModel.hideItemByNames: triggering fireTableDataChanged");
        fireTableDataChanged();
      }
    }

    public void unhideItemByType(DatabaseObjectType databaseObjectType) {
      LOGGER.debug("DatabaseObjectListTableModel.unhideItemByType: " + databaseObjectType);
      List<DBObjectItem> matches = new ArrayList<>();
      parkedItems.stream().filter(item -> item.getType().equals(databaseObjectType)).allMatch(
          match -> matches.add(match)
      );
      if (matches.size() > 0) {
        parkedItems.removeAll(matches);
        allItems.addAll(matches);
        LOGGER.debug("DatabaseObjectListTableModel.unhideItemByType: triggering fireTableDataChanged");
        fireTableDataChanged();
      }
    }


    /**
     * Hide a itme from the model by its names
     * A modle onyl contain item for a given schema, no name collision
     * can hapen
     *
     * @param itemNames name of the object in the model
     */
    public void hideItemByNames(List<String> itemNames) {
      if (LOGGER.isDebugEnabled())
        LOGGER.debug("DatabaseObjectListTableModel.hideItemByNames: " + itemNames);
      List<DBObjectItem> matches = new ArrayList<>();
      allItems.stream().filter(item -> itemNames.contains(item.getName())).allMatch(
          match -> matches.add(match)
      );

      if (matches.size() > 0) {
        parkedItems.addAll(matches);
        allItems.removeAll(matches);
        LOGGER.debug("DatabaseObjectListTableModel.hideItemByNames: triggering fireTableDataChanged");
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
     *
     * @param itemOwner item owner
     * @param itemName  item name, can be null meaning "all"
     */
    private void unhideItem(String itemOwner, String itemName) {
      // first deal with wild card
      // in that case, unhide all from the owner
      boolean removed = false;
      List<DBObjectItem> toBoMoved;
      if (itemName == null) {
        toBoMoved = parkedItems.stream().filter(
            o -> o.getOwner().equalsIgnoreCase(itemOwner)).collect(Collectors.toList());
      } else {
        toBoMoved = parkedItems.stream().filter(
            o -> o.getOwner().equalsIgnoreCase(itemOwner) &&
                o.getName().equalsIgnoreCase(itemName)).collect(Collectors.toList());

      }
      if (toBoMoved.size() > 0) {
        parkedItems.removeAll(toBoMoved);
        allItems.addAll(toBoMoved);
        fireTableDataChanged();
      }
    }

    public DBObjectItem getItemAt(int rowIndex) {
      return allItems.get(rowIndex);
    }
  }


  @Override
  public boolean onFinish() {
    boolean isValid = profileObjectListTable.getInputVerifier().verify(profileObjectListTable);

    if (isValid) {
      return true;
    } else {
      Messages.showErrorDialog(project, "Please add a table or view");
      return false;
    }
  }


}
