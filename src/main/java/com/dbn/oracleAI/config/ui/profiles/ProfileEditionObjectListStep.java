package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.common.icon.Icons;
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
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;

import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Profile edition Object list step for edition wizard
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionObjectListStep extends WizardStep<ProfileEditionWizardModel> {

  static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());
  private static final Logger LOGGER = Logger.getInstance("com.dbn.oracleAI");


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

  Map<String, DatabaseObjectListTableModel> databaseObjectListTableModelCache = new HashMap<>();

  public ProfileEditionObjectListStep(Project project, Profile profile, boolean isUpdate) {
    super(ResourceBundle.getBundle("Messages", Locale.getDefault()).getString("profile.mgmt.object_list_step.title"));
    this.profileSvc = project.getService(DatabaseOracleAIManager.class).getProfileService();
    this.project = project;
    this.profile = profile;
    this.isUpdate = isUpdate;
    this.databaseSvc = project.getService(DatabaseOracleAIManager.class).getDatabaseService();

    patternFilter.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        if (e.getDocument().getLength() > 2)
              filter();
      }

      public void removeUpdate(DocumentEvent e) {
        if (e.getDocument().getLength() > 2) filter();
      }

      public void insertUpdate(DocumentEvent e) {
        if (e.getDocument().getLength() > 2) filter();
      }

      public void filter() {
        populateDatabaseObjectTable(schemaComboBox.getSelectedItem().toString());
      }
    });

    withViewsButton.addItemListener((
        e -> {
          if (((JCheckBox) e.getSource()).isSelected()) {
            currentDbObjListTableModel.unhideItemByType(DatabaseObjectType.VIEW);
          } else {
            currentDbObjListTableModel.hideItemByType(DatabaseObjectType.VIEW);
          }

        }
    ));
    initializeTables();
    DBObjectsTransferHandler th = new DBObjectsTransferHandler();
    this.databaseObjectsTable.setTransferHandler(
        th
    );

    this.profileObjectListTable.setTransferHandler(
        th
    );

    this.databaseObjectsTable.setDragEnabled(true);
    this.databaseObjectsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    this.profileObjectListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.profileObjectListTable.setDragEnabled(true);
    this.profileObjectListTable.setFillsViewportHeight(true);
    this.profileObjectListTable.setDropMode(DropMode.INSERT_ROWS);

  }

  private void initializeTables() {
    LOGGER.debug("initializing tables");

    profileObjectListTable.setModel(profileObjListTableModel);

    //TODO : check this
    addValidationListener();

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
            DatabaseObjectListTableModel model = databaseObjectListTableModelCache.get(item.getOwner());
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
      Object toBePopulated = schemaComboBox.getSelectedItem();
      if (toBePopulated == null)
        toBePopulated = schemaComboBox.getItemAt(0);
      if (toBePopulated != null) {
        populateDatabaseObjectTable(toBePopulated.toString());
      }
    });

    loadSchemas();

    if (isUpdate) {
      SwingUtilities.invokeLater(() -> {
        profileObjListTableModel.updateItems(profile.getObjectList());
      });
    }
    ((TableRowSorter) databaseObjectsTable.getRowSorter()).setRowFilter(new RowFilter<DatabaseObjectListTableModel, Integer>() {

      @Override
      public boolean include(Entry<? extends DatabaseObjectListTableModel, ? extends Integer> entry) {

        // first : does it match selected schema ?
        if (!entry.getStringValue(TABLES_COLUMN_HEADERS_OWNER_IDX).equalsIgnoreCase(schemaComboBox.getSelectedItem().toString())) {
          return false;
        }

        // second : does it match the current patten if any
        if ((patternFilter.getText().length() > 0) && !entry.getStringValue(TABLES_COLUMN_HEADERS_NAME_IDX).matches(patternFilter.getText())) {
          return false;
        }
        return true;
      }
    });

    databaseObjectsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        DBObjectItem currentItem = currentDbObjListTableModel.getItemAt(row);
        if (currentItem.getType() == DatabaseObjectType.VIEW) {
          setIcon(Icons.DBO_VIEW);
        } else {
          setIcon(Icons.DBO_TABLE);
        }
        setText(value.toString());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        return this;
      }
    });

    profileObjectListTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        switch (column) {
          case ProfileObjectListTableModel.NAME_COLUMN_IDX:
            // Do not render icon etc... for owner cell
            // Check if the current row item is in the selectedTableModel
            setText((value != null) ? value.toString() : "*");
            ProfileDBObjectItem currentItem = profileObjListTableModel.getItemAt(row);
            DatabaseObjectType currentItemType;
            try {
              currentItemType = locateTypeFor(currentItem);
              if (locateTypeFor(currentItem) == DatabaseObjectType.VIEW) {
                setIcon(Icons.DBO_VIEW);
              } else {
                setIcon(Icons.DBO_TABLE);
              }
            } catch (IllegalStateException e) {
              setToolTipText(e.getMessage());
              setForeground(Color.RED);
            }
            break;
          case ProfileObjectListTableModel.OWNER_COLUMN_IDX:
            setText(value.toString());
            setIcon(null);
            break;
          default:
            assert false : "unexpected column number";
        }

        return this;
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
   * @return the type of that item or null if its a wildcard object (i.e name == null)
   * @throw IllegalStateException schema or object is not known to our model
   */
  private DatabaseObjectType locateTypeFor(ProfileDBObjectItem item) throws IllegalStateException {
    if (item.getName() == null || item.getName().length() == 0) {
      return null;
    }

    DatabaseObjectListTableModel model = databaseObjectListTableModelCache.get(item.getOwner());

    if (model == null) {
      // surely a schema we do not know.
      // that's possible as profile ae populated by name.
      // object list as no guaranty to exist
      throw new IllegalStateException(messages.getString("profile.mgmt.obj_list.unknown_schema"));
    }

    Optional<DBObjectItem> oitem = model.findFirst(item);
    if (oitem.isEmpty()) {
      // look for hidden ones then
      oitem = model.parkedItems.stream().filter(item::isEquivalentTo).findFirst();
    }
    if (oitem.isEmpty()) {
      throw new IllegalStateException(messages.getString("profile.mgmt.obj_list.unknown_obj"));
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

  private void addValidationListener() {
    // TODO : remove this

    profileObjectListTable.setInputVerifier(new SelectedObjectItemsVerifier());
    profileObjectListTable.getModel().addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.INSERT) {
        profileObjectListTable.getInputVerifier().verify(profileObjectListTable);
      }
    });
  }

  private void populateDatabaseObjectTable(String schema) {

    assert (schema != null && !schema.isEmpty()) : "Invalid schema passed";
    DatabaseObjectListTableModel model = databaseObjectListTableModelCache.get(schema);

    if (model == null) {
      startActivityNotifier();
      databaseObjectsTable.setEnabled(false);
      databaseSvc.getObjectItemsForSchema(schema).thenAccept(objs -> {
        SwingUtilities.invokeLater(() -> {
          DatabaseObjectListTableModel newModel = new DatabaseObjectListTableModel(objs, !withViewsButton.isSelected());
          databaseObjectListTableModelCache.put(schema, newModel);
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

  public @org.jetbrains.annotations.Nullable JComponent getPreferredFocusedComponent() {
    return null;
  }


  @Override
  public boolean onFinish() {
    if (profileObjectListTable.getInputVerifier().verify(profileObjectListTable)) {
      profile.setObjectList(profileObjListTableModel.getData());
    }
    return true;
  }

}
