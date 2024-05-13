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

import javax.swing.DropMode;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
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
public class ProfileEditionObjectListStep extends AbstractProfileEditionStep {

  static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());
  private static final Logger LOGGER = Logger.getInstance("com.dbn.oracleAI");

  private static final int TABLES_COLUMN_HEADERS_NAME_IDX = 0;
  private static final int TABLES_COLUMN_HEADERS_OWNER_IDX = 1;

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
  ProfileObjectListTableModel profileObjListTableModel = new ProfileObjectListTableModel();

  //At start initialize it with empty one
  DatabaseObjectListTableModel currentDbObjListTableModel = new DatabaseObjectListTableModel();

  Map<String,DatabaseObjectListTableModel> DatabaseObjectListTableModelCache = new HashMap<>();

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
          if (((JCheckBox)e.getSource()).isSelected()) {
            currentDbObjListTableModel.unhideItemByType(DatabaseObjectType.VIEW);
          } else {
            currentDbObjListTableModel.hideItemByType(DatabaseObjectType.VIEW);
          }

        }
    ));
    initializeTables(profile);
    DBObjectsTransferHandler th = new DBObjectsTransferHandler();
    this.databaseObjectsTable.setTransferHandler(
            th
    );

    this.profileObjectListTable.setTransferHandler(
            th
    );

    this.databaseObjectsTable.setDragEnabled(true);
    this.profileObjectListTable.setDragEnabled(true);
    this.profileObjectListTable.setFillsViewportHeight(true);
    this.profileObjectListTable.setDropMode(DropMode.INSERT_ROWS);

  }

  private void initializeTables(@Nullable Profile profile) {
    LOGGER.debug("initializing tables");
    profileObjectListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    profileObjectListTable.setModel(profileObjListTableModel);
    addValidationListener(profileObjectListTable);
    databaseObjectsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    databaseObjectsTable.setModel(currentDbObjListTableModel);
    databaseObjectsTable.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          JTable table = ((JTable)e.getSource());
          int [] selectedRows = table.getSelectedRows();
          if (selectedRows != null ) {
            // hide all and add to profile obj list
            List<String> namesTobeHidden = new ArrayList<>(selectedRows.length);
            List<ProfileDBObjectItem> newItems = new ArrayList<>(selectedRows.length);

            for (int i = 0;i<selectedRows.length;i++) {
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
            DatabaseObjectListTableModel model = DatabaseObjectListTableModelCache.get(item.getOwner());
            assert model != null: "trying to unhide items form model not in the cache";
            // TODO : verify that this do nto fire event when this is not the current model
            //        of the table
            model.unhideItem(item.getOwner(),item.getName());
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

    if (profile !=null) {
      profileObjListTableModel.updateItems(profile.getObjectList());
    }
    ((TableRowSorter)databaseObjectsTable.getRowSorter()).setRowFilter(new RowFilter<DatabaseObjectListTableModel, Integer>() {

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

    DatabaseObjectListTableModel model = DatabaseObjectListTableModelCache.get(item.getOwner());
    Optional<DBObjectItem> oitem = model.findFirst(item);
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
      ApplicationManager.getApplication().invokeLater(() -> {
        for (String schema : schemaList) {
          LOGGER.debug("Adding new schema to dropbox: " + schema);
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

  private void populateDatabaseObjectTable(String schema) {
    DatabaseObjectListTableModel model  = DatabaseObjectListTableModelCache.get(schema);
    if (model == null) {
      startActivityNotifier();
      databaseObjectsTable.setEnabled(false);
      databaseSvc.getObjectItemsForSchema(schema).thenAccept(objs->{
        ApplicationManager.getApplication().invokeLater(() -> {
          DatabaseObjectListTableModel newModel = new DatabaseObjectListTableModel(objs,!withViewsButton.isSelected());
          DatabaseObjectListTableModelCache.put(schema,newModel);
          databaseObjectsTable.setModel(newModel);
          currentDbObjListTableModel = newModel;
          stopActivityNotifier();
          databaseObjectsTable.setEnabled(true);
        });
      }).exceptionally(e -> {
        Messages.showErrorDialog(project,"Cannot fetch objects",
                "Cannot fetching database object list: "+e.getCause().getMessage());
        return null;
      });
    } else {
      currentDbObjListTableModel = model;
      databaseObjectsTable.setModel(model);
    }
  }


  @Override
  public JPanel getPanel() {
    return profileEditionObjectListMainPane;
  }

  @Override
  public void setAttributesOn(Profile p) {
    p.setObjectList(profileObjListTableModel.getData());
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
