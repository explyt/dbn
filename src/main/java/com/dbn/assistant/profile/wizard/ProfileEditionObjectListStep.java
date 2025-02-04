/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.assistant.profile.wizard;

import com.dbn.assistant.profile.wizard.validation.ProfileObjectsVerifier;
import com.dbn.common.color.Colors;
import com.dbn.common.text.TextContent;
import com.dbn.common.thread.Background;
import com.dbn.common.ui.ValueSelectorOption;
import com.dbn.common.ui.form.DBNHintForm;
import com.dbn.common.ui.misc.DBNComboBox;
import com.dbn.common.ui.util.Accessibility;
import com.dbn.common.ui.util.Borders;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.common.util.Actions;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.object.DBDataset;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObject;
import com.dbn.object.common.DBObjectBundle;
import com.dbn.object.lookup.DBObjectRef;
import com.dbn.object.type.DBObjectType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.dbn.common.text.TextContent.plain;
import static com.dbn.common.ui.table.Tables.adjustTableRowHeight;
import static com.dbn.common.ui.table.Tables.installFocusTraversal;
import static com.dbn.common.ui.table.Tables.selectTableRow;
import static com.dbn.common.ui.util.Keyboard.onKeyPress;
import static com.dbn.common.ui.util.Mouse.onMouseClick;
import static com.dbn.common.ui.util.TextFields.onTextChange;
import static com.dbn.common.util.Commons.nvl;
import static com.dbn.nls.NlsResources.txt;

/**
 * Profile edition Object list step for edition wizard
 *
 * @see ProfileEditionWizard
 */
@Slf4j
public class ProfileEditionObjectListStep extends WizardStep<ProfileEditionWizardModel> implements Disposable {
  private JPanel mainPanel;
  private JBTextField filterTextField;
  private JTable profileObjectListTable;
  private JTable databaseObjectsTable;
  private DBNComboBox<DBSchema> schemaComboBox;
  private JPanel actionsPanel;
  private JPanel hintPanel;
  private JPanel initializingIconPanel;

  private final ConnectionRef connection;
  private final ProfileData profile;

  ObjectsTableModel objectsTableModel = new ObjectsTableModel();

/*
  //At start initialize it with empty one
  DatabaseObjectListTableModel currentDbObjListTableModel = new DatabaseObjectListTableModel();
  TableRowSorter<DatabaseObjectListTableModel> databaseObjectsTableSorter = new TableRowSorter<>();
  Map<String, DatabaseObjectListTableModel> databaseObjectListTableModelCache = new HashMap<>();
*/

  public ProfileEditionObjectListStep(ConnectionHandler connection, ProfileData profile, boolean isUpdate) {
    super(txt("cfg.assistant.title.ProfileObjectSettings"),
        txt("cfg.assistant.text.ProfileObjectSettings"));

    this.connection = connection.ref();

    this.profile = profile;

    initHintPanel();
    initObjectTables();
    initActionToolbar();
    initSchemaSelector();
    initFilterField();
    initAccessibility();

    objectsTableModel.addTableModelListener(l -> updateDatasetsFilter());

    if (isUpdate) {
      SwingUtilities.invokeLater(() -> {
        objectsTableModel.updateItems(profile.getObjectList());
      });
    }
    UserInterface.updateSplitPanes(mainPanel);
  }

  private void initAccessibility() {
    Accessibility.setAccessibleName(databaseObjectsTable, "Available Datasets");
    Accessibility.setAccessibleName(profileObjectListTable, "Profile Datasets");
  }

  private void initHintPanel() {
    TextContent hintText = plain("AI-Profiles must include information about your data model to be forwarded to the language model. This will allow it to produce more accurate results, closely tailored to your data model. " +
            "The metadata can include database table names, column names, column data types, and comments. Your data will never be sent out to the language model.\n\n" +
            "Please find the datasets you want to include in the profile, and drag them to the container on the right. Start by selecting the schema.");
    DBNHintForm hintForm = new DBNHintForm(null, hintText, null, true);

    JComponent hintComponent = hintForm.getComponent();
    hintPanel.add(hintComponent);

  }

  private void initSchemaSelector() {
    schemaComboBox.set(ValueSelectorOption.HIDE_DESCRIPTION, true);
    schemaComboBox.addListener((ov, nv) -> populateDatabaseObjectTable(nv));
    loadSchemas();
  }

  private void initFilterField() {
    filterTextField.getEmptyText().setText("Filter");
    onTextChange(filterTextField, e -> updateDatasetsFilter());
  }

  protected void initActionToolbar() {
    Supplier<Set<DBObjectType>> selectedDatasetTypes = () -> getDatasetFilter().getObjectTypes();
    Runnable toggleCallback = () -> updateDatasetsFilter();

    ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel, true,
            DatasetTypeToggleAction.create(DBObjectType.TABLE, selectedDatasetTypes, toggleCallback),
            DatasetTypeToggleAction.create(DBObjectType.VIEW, selectedDatasetTypes, toggleCallback),
            DatasetTypeToggleAction.create(DBObjectType.MATERIALIZED_VIEW, selectedDatasetTypes, toggleCallback));

    Accessibility.setAccessibleName(actionToolbar, "Dataset Type Filters");

    JComponent component = actionToolbar.getComponent();
    component.setOpaque(false);
    component.setBorder(Borders.EMPTY_BORDER);
    actionsPanel.add(component, BorderLayout.CENTER);
    actionsPanel.setBorder(JBUI.Borders.empty(4));

    initializingIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);
  }


  private void updateDatasetsFilter() {
    AvailableDatasetsTableModel model = getDatasetsModel();
    AvailableDatasetsFilter filter = getDatasetFilter();
    filter.setNameToken(filterTextField.getText());

    Set<String> selectedElements = filter.getSelectedElements();
    selectedElements.clear();
    List<DBObjectRef<DBObject>> data = objectsTableModel.getData();
    data.forEach(d -> selectedElements.add(d.getSchemaName() + "." + d.getObjectName()));

    model.notifyDataChanges();
  }

  private AvailableDatasetsFilter getDatasetFilter() {
    return getDatasetsModel().getFilter();
  }

  private AvailableDatasetsTableModel getDatasetsModel() {
    return (AvailableDatasetsTableModel) databaseObjectsTable.getModel();
  }

  private ConnectionHandler getConnection() {
    return ConnectionRef.ensure(connection);
  }

  private Project getProject() {
    return getConnection().getProject();
  }

  private void initObjectTables() {
    ProfileObjectsTransferHandler th = new ProfileObjectsTransferHandler();

    initializeDatabaseObjectTable(th);
    initializeProfileObjectTable(th);
  }

  private void initializeDatabaseObjectTable(ProfileObjectsTransferHandler th) {
    this.databaseObjectsTable.setAutoCreateRowSorter(false);
    this.databaseObjectsTable.setTransferHandler(th);
    this.databaseObjectsTable.setModel(new AvailableDatasetsTableModel());
    this.databaseObjectsTable.setTableHeader(null);

    this.databaseObjectsTable.setDragEnabled(true);
    this.databaseObjectsTable.setBackground(Colors.getTextFieldBackground());
    this.databaseObjectsTable.setGridColor(Colors.getTextFieldBackground());
    this.databaseObjectsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    adjustTableRowHeight(databaseObjectsTable, 1);
    installFocusTraversal(databaseObjectsTable);
    onMouseClick(databaseObjectsTable, MouseEvent.BUTTON1, 2, e -> selectDatabaseObject());
    onKeyPress(databaseObjectsTable, KeyEvent.VK_SPACE, e -> selectDatabaseObject());

    this.databaseObjectsTable.setDefaultRenderer(DBDataset.class,
            new ColoredTableCellRenderer() {
              @Override
              protected void customizeCellRenderer(@NotNull JTable table, @Nullable Object value, boolean selected, boolean hasFocus, int row, int column) {
                if (value == null) return;

                DBDataset dataset = (DBDataset) value;
                setIcon(dataset.getIcon());
                append(dataset.getName());
                setBorder(Borders.EMPTY_BORDER);
              }
            }
    );
  }

  private void initializeProfileObjectTable(ProfileObjectsTransferHandler th) {
    this.profileObjectListTable.setTransferHandler(th);

    this.profileObjectListTable.setModel(objectsTableModel);
    this.profileObjectListTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    this.profileObjectListTable.setTableHeader(null);
    this.profileObjectListTable.setBackground(Colors.getTextFieldBackground());
    this.profileObjectListTable.setGridColor(Colors.getTextFieldBackground());

    adjustTableRowHeight(profileObjectListTable, 1);
    installFocusTraversal(profileObjectListTable);
    onMouseClick(profileObjectListTable, MouseEvent.BUTTON1, 2, e -> deselectDatabaseObject());
    onKeyPress(profileObjectListTable, KeyEvent.VK_SPACE, e -> deselectDatabaseObject());

    profileObjectListTable.setDefaultRenderer(Object.class, new ColoredTableCellRenderer() {
      @Override
      protected void customizeCellRenderer(@NotNull JTable table, @Nullable Object value, boolean selected, boolean hasFocus, int row, int column) {
        if (value == null) return;

        DBObject profileItem = (DBObject) value;
        append(nvl(profileItem.getSchemaName(), ""));
        append(".");
        append(profileItem.getName());
        setBorder(Borders.EMPTY_BORDER);

        DBObjectType objectType = profileItem.getObjectType();
        setIcon(objectType.getIcon());
      }
    });

    profileObjectListTable.setInputVerifier(new ProfileObjectsVerifier());
    profileObjectListTable.getModel().addTableModelListener(e -> {
      if (e.getType() == TableModelEvent.INSERT) {
        profileObjectListTable.getInputVerifier().verify(profileObjectListTable);
      }
    });
  }

  private void selectDatabaseObject() {
    int selectedRow = databaseObjectsTable.getSelectedRow();
    if (selectedRow < 0) return;

    DBDataset dataset = (DBDataset) databaseObjectsTable.getModel().getValueAt(selectedRow, 0);
    objectsTableModel.addItems(List.of(dataset));
    selectTableRow(databaseObjectsTable, selectedRow);
  }

  private void deselectDatabaseObject() {
    int selectedRow = profileObjectListTable.getSelectedRow();
    if (selectedRow < 0) return;

    objectsTableModel.removeItem(selectedRow);
    selectTableRow(profileObjectListTable, selectedRow);
  }

  private void startActivityNotifier() {
    initializingIconPanel.setVisible(true);
  }

  /**
   * Stops the spining wheel
   */
  private void stopActivityNotifier() {
    initializingIconPanel.setVisible(false);
  }

  private void loadSchemas() {
    Background.run(() -> {
      try {
        startActivityNotifier();
        DBObjectBundle objectBundle = getConnection().getObjectBundle();
        List<DBSchema> schemas = objectBundle.getSchemas(true);
        DBSchema schema = objectBundle.getUserSchema();
        schemaComboBox.setValues(schemas);
        schemaComboBox.setSelectedValue(schema);

      } finally {
        stopActivityNotifier();
      }
    });

  }

  private void populateDatabaseObjectTable(DBSchema schema) {
    if (schema == null) return;
    Background.run(() -> {
      try {
        startActivityNotifier();
        AvailableDatasetsTableModel model = getDatasetsModel();
        model.setDatasets(Collections.emptyList());
        model.notifyDataChanges();

        // long-lasting load process
        List<DBDataset> datasets = schema.getDatasets();

        // verify if schema selection changed meanwhile
        if (schema != schemaComboBox.getSelectedValue()) return;

        model.setDatasets(datasets);
        updateDatasetsFilter();
      } finally {
        stopActivityNotifier();
      }
    });
  }


  @Override
  public @Nullable String getHelpId() {
    return null;
  }

  @Override
  public JComponent prepare(WizardNavigationState wizardNavigationState) {
    return mainPanel;
  }

  @Override
  public boolean onFinish() {
    if (profileObjectListTable.getInputVerifier().verify(profileObjectListTable)) {
      profile.setObjectList(objectsTableModel.getData());
    }
    return true;
  }

  @Override
  public void dispose() {
    // TODO dispose UI resources
  }
}
