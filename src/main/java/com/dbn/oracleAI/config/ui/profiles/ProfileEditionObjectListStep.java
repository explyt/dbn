package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.DatabaseService;
import com.dbn.oracleAI.ManagedObjectServiceProxy;
import com.dbn.oracleAI.ProfileEditionWizard;
import com.dbn.oracleAI.config.DBObjectItem;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileDBObjectItem;
import com.dbn.oracleAI.config.ui.SelectedObjectItemsVerifier;
import com.dbn.oracleAI.types.DatabaseObjectType;
import com.dbn.oracleAI.ui.ActivityNotifier;
import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.dbn.nls.NlsResources.txt;

/**
 * Profile edition Object list step for edition wizard
 *
 * @see ProfileEditionWizard
 */
@Slf4j
public class ProfileEditionObjectListStep extends WizardStep<ProfileEditionWizardModel> {


  private static final int TABLES_COLUMN_HEADERS_NAME_IDX = 0;
  private static final int TABLES_COLUMN_HEADERS_OWNER_IDX = 1;

  private static final String[] PROFILE_OBJ_TABLES_COLUMN_HEADERS = {
      txt("profile.mgmt.obj_table.header.name"),
      txt("profile.mgmt.obj_table.header.owner")
  };
  private static final String[] DB_OBJ_TABLES_COLUMN_HEADERS = {
      txt("profile.mgmt.obj_table.header.name")
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
  private final ManagedObjectServiceProxy<Profile> profileSvc;
  private final DatabaseService databaseSvc;
  private final Project project;
  private final Profile profile;
  private final boolean isUpdate;

  ProfileObjectListTableModel profileObjListTableModel = new ProfileObjectListTableModel();

  //At start initialize it with empty one
  DatabaseObjectListTableModel currentDbObjListTableModel = new DatabaseObjectListTableModel();
  TableRowSorter<DatabaseObjectListTableModel> databaseObjectsTableSorter = new TableRowSorter<>();
  Map<String, DatabaseObjectListTableModel> databaseObjectListTableModelCache = new HashMap<>();

  public ProfileEditionObjectListStep(Project project, Profile profile, boolean isUpdate) {
    super(txt("profile.mgmt.object_list_step.title"),
        txt("profile.mgmt.object_list_step.explaination"),
        Icons.DB_GENERIC);
    this.profileSvc = project.getService(DatabaseOracleAIManager.class).getProfileService();
    this.project = project;
    this.profile = profile;
    this.isUpdate = isUpdate;
    this.databaseSvc = project.getService(DatabaseOracleAIManager.class).getDatabaseService();

    if (this.profile != null)
      prefetchObjectForProfile(this.profile);

    initializeTables();

    schemaComboBox.addActionListener((e) -> {
      log.debug("action listener on  schemaComboBox fired");
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
    patternFilter.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        // experience showed that's never called
        assert false : "changedUpdate called??";
      }

      public void removeUpdate(DocumentEvent e) {
        if (log.isDebugEnabled())
          log.debug("patternFilter.removeUpdate doc length: " + e.getDocument().getLength());
        String filter = null;
        try {
          filter = e.getDocument().getText(0, e.getDocument().getLength()).trim();
        } catch (BadLocationException ignored) {
          log.debug("BadLocationException", ignored);
        }
        if (filter.isEmpty()) {
          // filter cleared
          triggerFiltering();
        }
        if (filter.length() > 2) {
          // filter cleared
          triggerFiltering();
        }
      }

      public void insertUpdate(DocumentEvent e) {
        if (log.isDebugEnabled())
          log.debug("patternFilter.insertUpdate doc length: " + e.getDocument().getLength());
        try {
          if (e.getDocument().getText(0, e.getDocument().getLength()).trim().length() > 2) {
            triggerFiltering();
          }
        } catch (BadLocationException ignored) {
          log.debug("BadLocationException", ignored);
        }
      }

      public void triggerFiltering() {
        if (log.isDebugEnabled()) {
          log.debug("triggering  fireTableDataChanged on " + ((DatabaseObjectListTableModel) databaseObjectsTable.getModel()));
          log.debug("    current model " + currentDbObjListTableModel);
        }
        currentDbObjListTableModel.fireTableDataChanged();
      }
    });

    withViewsButton.addItemListener((
        e -> {
          if (((JCheckBox) e.getSource()).isSelected()) {
            currentDbObjListTableModel.unhideItemByType(DatabaseObjectType.VIEW);
            currentDbObjListTableModel.unhideItemByType(DatabaseObjectType.MATERIALIZED_VIEW);
          } else {
            currentDbObjListTableModel.hideItemByType(DatabaseObjectType.VIEW);
            currentDbObjListTableModel.hideItemByType(DatabaseObjectType.MATERIALIZED_VIEW);
          }

        }
    ));

    selectAllCheckBox.addItemListener((
        e -> {
          if (((JCheckBox) e.getSource()).isSelected()) {
            databaseObjectsTable.selectAll();
          } else {
            databaseObjectsTable.clearSelection();
          }

        }
    ));

  }

  private Set<String> schemaInPrefetch = new HashSet<>();

  private void prefetchObjectForProfile(Profile profile) {
    schemaInPrefetch.clear();
    // TODO : implement bulk fetch
    profile.getObjectList().stream()
        .map(profileDBObjectItem -> profileDBObjectItem.getOwner())
        .distinct().forEach(schemaName -> {
          if (!databaseObjectListTableModelCache.containsKey(schemaName)) {
            log.debug("prefetching schema : " + schemaName);
            schemaInPrefetch.add(schemaName.toLowerCase());
            databaseSvc.getObjectItemsForSchema(schemaName).thenAccept(objs -> {
              DatabaseObjectListTableModel newModel = new DatabaseObjectListTableModel(objs, !withViewsButton.isSelected());
              log.debug("new schema prefetched: " + schemaName + " obj count: " + objs.size());
              newModel.hideItemByNames(
                  this.profile.getObjectList().stream().filter(o -> o.getOwner().equalsIgnoreCase(schemaName)).map(o -> o.getName()).collect(Collectors.toList()));

              databaseObjectListTableModelCache.put(schemaName, newModel);
              schemaInPrefetch.remove(schemaName.toLowerCase());
              this.profileObjectListTable.repaint();
            });
          }
        });
  }

  private void initializeTables() {
    log.debug("initializing tables");

    DBObjectsTransferHandler th = new DBObjectsTransferHandler();

    initializeDatabaseObjectTable(th);
    initializeProfileObjectTable(th);
  }

  private void resetDatabaseObjectTableModel(DatabaseObjectListTableModel m) {
    log.debug("resetDatabaseObjectTableModel for " + m);
    this.databaseObjectsTable.setModel(m);
    this.databaseObjectsTableSorter.setModel(m);
  }

  private void initializeDatabaseObjectTable(DBObjectsTransferHandler th) {
    log.debug("initializing databaseObjectsTable");
    // keep this !
    // if set to true a RowSorter is created each the model changes
    // and that breaks our logic
    this.databaseObjectsTable.setAutoCreateRowSorter(false);

    this.databaseObjectsTable.setTransferHandler(th);
    resetDatabaseObjectTableModel(currentDbObjListTableModel);
    this.databaseObjectsTableSorter.setRowFilter(new RowFilter<>() {

      @Override
      public boolean include(Entry<? extends DatabaseObjectListTableModel, ? extends Integer> entry) {
        String currentFilter = patternFilter.getText().trim().toLowerCase();
        // by default we show the entry
        boolean shallInclude = true;
        if (!currentFilter.isEmpty()) {
          //shallInclude = entry.getStringValue(TABLES_COLUMN_HEADERS_NAME_IDX).matches(currentFilter);
          // keep it simple for now
          shallInclude = entry.getStringValue(TABLES_COLUMN_HEADERS_NAME_IDX).toLowerCase().contains(currentFilter);
        }
        if (log.isDebugEnabled()) {
          log.debug(this + " filtering model " + entry.getModel() + " on [" + currentFilter + "] for [" +
              entry.getStringValue(TABLES_COLUMN_HEADERS_NAME_IDX) + "] => " + shallInclude);
        }
        return shallInclude;
      }
    });
    this.databaseObjectsTable.setRowSorter(this.databaseObjectsTableSorter);
    this.databaseObjectsTable.setDragEnabled(true);
    this.databaseObjectsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    this.databaseObjectsTable.addMouseListener(new MouseAdapter() {
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
    this.databaseObjectsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
    log.debug("initialization databaseObjectsTable complete");
  }

  private void initializeProfileObjectTable(DBObjectsTransferHandler th) {
    log.debug("initializing profileObjectListTable");
    this.profileObjectListTable.setTransferHandler(th);

    this.profileObjectListTable.setModel(profileObjListTableModel);
    this.profileObjectListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.profileObjectListTable.setDragEnabled(true);
    this.profileObjectListTable.setFillsViewportHeight(true);
    this.profileObjectListTable.setDropMode(DropMode.INSERT_ROWS);
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
              setFont(getFont().deriveFont(Font.PLAIN));
              setToolTipText(null);
              if (currentItemType == DatabaseObjectType.VIEW) {
                setIcon(Icons.DBO_VIEW);
              } else {
                setIcon(Icons.DBO_TABLE);
              }
            } catch (IllegalStateException e) {
              if (schemaInPrefetch.contains(currentItem.getOwner().toLowerCase())) {
                setToolTipText(txt("profile.mgmt.object.information.loading"));
                setFont(getFont().deriveFont(Font.ITALIC));
              } else {
                setToolTipText(e.getMessage());
                setForeground(Color.RED);
              }
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
    profileObjectListTable.setInputVerifier(new SelectedObjectItemsVerifier());
    profileObjectListTable.getModel().addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.INSERT) {
        profileObjectListTable.getInputVerifier().verify(profileObjectListTable);
      }
    });
    log.debug("initialization profileObjectListTable complete");
  }

  private void startActivityNotifier() {
    ((ActivityNotifier) activityProgress).start();
  }

  /**
   * Stops the spining wheel
   */
  private void stopActivityNotifier() {
    ((ActivityNotifier) activityProgress).stop();
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
      throw new IllegalStateException(txt("profile.mgmt.obj_list.unknown_schema"));
    }

    Optional<DBObjectItem> oitem = model.findFirst(item);
    if (oitem.isEmpty()) {
      // look for hidden ones then
      oitem = model.parkedItems.stream().filter(item::isEquivalentTo).findFirst();
    }
    if (oitem.isEmpty()) {
      throw new IllegalStateException(txt("profile.mgmt.obj_list.unknown_obj"));
    }
    // at this point, no way to no have something
    return oitem.get().getType();
  }

  private void loadSchemas() {
    log.debug("Loading schemas...");
    startActivityNotifier();
    databaseSvc.getSchemaNames().thenAccept(schemaList -> {
      for (String schema : schemaList) {
        log.debug("Adding new schema to dropbox: " + schema);
        schemaComboBox.addItem(schema);
      }
      stopActivityNotifier();
    }).exceptionally(e -> {
      Messages.showErrorDialog(project, "Cannot load schemas",
          "Cannot load DB schemas: " + e.getCause().getMessage());
      return null;
    });
  }

  private void populateDatabaseObjectTable(String schema) {
    assert (schema != null && !schema.isEmpty()) : "Invalid schema passed";

    log.debug("populateDatabaseObjectTable for " + schema);

    DatabaseObjectListTableModel model = databaseObjectListTableModelCache.get(schema);

    if (model == null) {
      log.debug("populateDatabaseObjectTable no cache for " + schema);
      startActivityNotifier();
      databaseObjectsTable.setEnabled(false);
      databaseSvc.getObjectItemsForSchema(schema).thenAccept(objs -> {
        SwingUtilities.invokeLater(() -> {
          if (log.isDebugEnabled())
            log.debug("populateDatabaseObjectTable new model for " + schema + " objs count=" + objs.size());
          DatabaseObjectListTableModel newModel = new DatabaseObjectListTableModel(objs, !withViewsButton.isSelected());
          // remove the one already selected
          newModel.hideItemByNames(
              this.profile.getObjectList().stream().filter(o -> o.getOwner().equalsIgnoreCase(schema)).map(o -> o.getName()).collect(Collectors.toList()));
          databaseObjectListTableModelCache.put(schema, newModel);
          currentDbObjListTableModel = newModel;
          resetDatabaseObjectTableModel(currentDbObjListTableModel);
          stopActivityNotifier();
          databaseObjectsTable.setEnabled(true);
        });
      }).exceptionally(e -> {
        Messages.showErrorDialog(project, "Cannot fetch objects",
            "Cannot fetch database object list: " + e.getCause().getMessage());
        return null;
      });
    } else {
      currentDbObjListTableModel = model;
      resetDatabaseObjectTableModel(currentDbObjListTableModel);
    }
  }


  @Override
  public @Nullable String getHelpId() {
    return null;
  }

  @Override
  public JComponent prepare(WizardNavigationState wizardNavigationState) {
    return profileEditionObjectListMainPane;
  }

  public @Nullable JComponent getPreferredFocusedComponent() {
    return null;
  }


  @Override
  public boolean onFinish() {
    if (profileObjectListTable.getInputVerifier().verify(profileObjectListTable)) {
      profile.setObjectList(profileObjListTableModel.getData());
    }
    return true;
  }

  private void createUIComponents() {
    activityProgress = new ActivityNotifier();
  }
}
