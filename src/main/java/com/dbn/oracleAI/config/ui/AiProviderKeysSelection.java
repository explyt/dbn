package com.dbn.oracleAI.config.ui;

import com.dbn.oracleAI.config.AIProviders.AIProviderType;
import com.dbn.oracleAI.config.AIProviders.AIProviderTypeBundle;
import com.dbn.oracleAI.config.AIProviders.AIProvidersGeneralSettings;
import com.dbn.oracleAI.config.AIProviders.AIProvidersSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

/**
 * This class is for a window that lists the available ai provider keys,
 * and allows us to select one and hydrate the createCredentialWindow with it.
 */
public class AiProviderKeysSelection extends DialogWrapper {
  private JPanel panel1;
  private JTable table1;
  private JTextField textField1;
  private AIProviderTypeBundle aiProviderTypes;
  private ProvidersSelectionCallback callback;


  protected AiProviderKeysSelection(@Nullable Project project, ProvidersSelectionCallback callback) {
    super(project, false);
    this.callback = callback;
    initializeProvidersList(project);
    setTitle("AI Provider Keys Selection");
    init();
    pack();
  }

  private void initializeProvidersList(Project project) {
    AIProvidersGeneralSettings settings = AIProvidersSettings.getInstance(project).getGeneralSettings();
    aiProviderTypes = settings.getAIProviderTypes();

    AIProviderTableModel model = new AIProviderTableModel(aiProviderTypes);
    table1.setModel(model);
  }

  private static class AIProviderTableModel extends AbstractTableModel {

    private final String HOSTNAME = "Hostname";
    private final String USERNAME = "Username";
    private final String KEY = "Key";
    private final AIProviderTypeBundle aiProviderTypes;
    private final String[] columnNames = {HOSTNAME, USERNAME, KEY};

    public AIProviderTableModel(AIProviderTypeBundle aiProviderTypes) {
      this.aiProviderTypes = aiProviderTypes;
    }

    @Override
    public int getRowCount() {
      return aiProviderTypes.size();
    }

    @Override
    public int getColumnCount() {
      return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      AIProviderType provider = aiProviderTypes.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return provider.getHostname();
        case 1:
          return provider.getUsername();
        case 2:
          return provider.getKey();
        default:
          return null;
      }
    }

    @Override
    public String getColumnName(int column) {
      return columnNames[column];
    }
  }

  @Override
  protected void doOKAction() {
    super.doOKAction();
    doSelectAction();
  }


  private void doSelectAction() {
    int selectedRow = table1.getSelectedRow();
    if (selectedRow >= 0) {
      AIProviderType selectedProvider = aiProviderTypes.get(selectedRow);
      callback.onProviderSelected(selectedProvider);
    }
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return panel1;
  }
}
