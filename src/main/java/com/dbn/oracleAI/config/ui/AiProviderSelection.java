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
import javax.swing.table.AbstractTableModel;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class is for a window that lists the available ai provider keys,
 * and allows us to select one and hydrate the createCredentialWindow with it.
 */
public class AiProviderSelection extends DialogWrapper {

  static private final ResourceBundle messages =
      ResourceBundle.getBundle("Messages", Locale.getDefault());
  private JPanel panel1;
  private JTable table1;
  private AIProviderTypeBundle aiProviderTypes;
  private ProvidersSelectionCallback callback;


  protected AiProviderSelection(@Nullable Project project, ProvidersSelectionCallback callback) {
    super(project, false);
    this.callback = callback;
    initializeProvidersList(project);
    setTitle(messages.getString("ai.settings.credential.template.window.title"));
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

    private final String NAME = "Provider Credential Name";
    private final String USERNAME = "Username";
    private final String KEY = "Secret";
    private final AIProviderTypeBundle aiProviderTypes;
    private final String[] columnNames = {NAME, USERNAME, KEY};

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
          String text = provider.getKey();
          if (text.length() > 4) {
            text = text.substring(0, 4) + "************";
          }
          return text;
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
