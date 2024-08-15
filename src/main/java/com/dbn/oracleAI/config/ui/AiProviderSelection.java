package com.dbn.oracleAI.config.ui;

import com.dbn.oracleAI.config.AIProviders.AIProviderCredential;
import com.dbn.oracleAI.config.AIProviders.AIProviderCredentialBundle;
import com.dbn.oracleAI.config.AIProviders.AIProviderCredentialGeneralSettings;
import com.dbn.oracleAI.config.AIProviders.AIProvidersSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import static com.dbn.nls.NlsResources.txt;

/**
 * This class is for a window that lists the available ai provider keys,
 * and allows us to select one and hydrate the createCredentialWindow with it.
 */
public class AiProviderSelection extends DialogWrapper {

  private JPanel panel1;
  private JTable table1;
  private AIProviderCredentialBundle aiProviderTypes;
  private ProvidersSelectionCallback callback;


  protected AiProviderSelection(@Nullable Project project, ProvidersSelectionCallback callback) {
    super(project, false);
    this.callback = callback;
    initializeProvidersList(project);
    setTitle(txt("ai.settings.credential.template.window.title"));
    init();
    pack();
  }

  private void initializeProvidersList(Project project) {
    AIProviderCredentialGeneralSettings settings = AIProvidersSettings.getInstance(project).getGeneralSettings();
    aiProviderTypes = settings.getAIProviderTypes();

    AIProviderTableModel model = new AIProviderTableModel(aiProviderTypes);
    table1.setModel(model);
  }

  private static class AIProviderTableModel extends AbstractTableModel {

    private final String NAME = "Provider Credential Name";
    private final int NAME_IDX = 0;
    private final String USERNAME = "Username";
    private final int USERNAME_IDX = 1;
    private final String KEY = "Secret";
    private final int KEY_IDX = 2;
    private final AIProviderCredentialBundle aiProviderTypes;
    private final String[] columnNames = {NAME, USERNAME, KEY};

    public AIProviderTableModel(AIProviderCredentialBundle aiProviderTypes) {
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
      AIProviderCredential provider = aiProviderTypes.get(rowIndex);
      switch (columnIndex) {
        case NAME_IDX:
          return provider.getCredentialName();
        case USERNAME_IDX:
          return provider.getUsername();
        case KEY_IDX:
          return "************";
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
      AIProviderCredential selectedProvider = aiProviderTypes.get(selectedRow);
      callback.onProviderSelected(selectedProvider);
    }
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return panel1;
  }
}
