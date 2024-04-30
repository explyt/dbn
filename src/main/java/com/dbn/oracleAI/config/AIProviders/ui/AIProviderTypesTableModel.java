package com.dbn.oracleAI.config.AIProviders.ui;

import com.dbn.common.project.ProjectRef;
import com.dbn.common.ui.table.DBNEditableTableModel;
import com.dbn.common.util.Commons;
import com.dbn.common.util.Strings;
import com.dbn.oracleAI.config.AIProviders.AIProviderType;
import com.dbn.oracleAI.config.AIProviders.AIProviderTypeBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AIProviderTypesTableModel extends DBNEditableTableModel {
  private AIProviderTypeBundle aiProviderTypes;
  private final ProjectRef project;

  AIProviderTypesTableModel(Project project, AIProviderTypeBundle environmentTypes) {
    this.project = ProjectRef.of(project);
    this.aiProviderTypes = new AIProviderTypeBundle(environmentTypes);
  }

  public AIProviderTypeBundle getAiProviderTypes() {
    return aiProviderTypes;
  }

  public void setAiProviderTypes(AIProviderTypeBundle environmentTypes) {
    this.aiProviderTypes = new AIProviderTypeBundle(environmentTypes);
    notifyListeners(0, environmentTypes.size(), -1);
  }

  @Override
  public int getRowCount() {
    return aiProviderTypes.size();
  }

  @NotNull
  public Project getProject() {
    return project.ensure();
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return columnIndex == 0 ? "Provider credential name" :
        columnIndex == 1 ? "UserName" :
            columnIndex == 2 ? "Secret" : null;
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnIndex == 0 ? String.class :
        columnIndex == 1 ? String.class :
            columnIndex == 2 ? String.class : null;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    AIProviderType environmentType = getAIProviderType(rowIndex);
    return
        columnIndex == 0 ? environmentType.getHostname() :
            columnIndex == 1 ? environmentType.getUsername() :
                columnIndex == 2 ? environmentType.getKey() : null;
  }

  @Override
  public void setValueAt(Object o, int rowIndex, int columnIndex) {
    Object actualValue = getValueAt(rowIndex, columnIndex);
    if (!Commons.match(actualValue, o)) {
      AIProviderType environmentType = aiProviderTypes.get(rowIndex);
      if (columnIndex == 0) {
        environmentType.setHostname((String) o);
      } else if (columnIndex == 1) {
        environmentType.setUsername((String) o);
      } else if (columnIndex == 2) {
        environmentType.setKey((String) o);
      }

      notifyListeners(rowIndex, rowIndex, columnIndex);
    }
  }

  private AIProviderType getAIProviderType(int rowIndex) {
    while (aiProviderTypes.size() <= rowIndex) {
      aiProviderTypes.add(new AIProviderType());
    }
    return aiProviderTypes.get(rowIndex);
  }

  @Override
  public void insertRow(int rowIndex) {
    aiProviderTypes.add(rowIndex, new AIProviderType());
    notifyListeners(rowIndex, aiProviderTypes.size() - 1, -1);
  }

  @Override
  public void removeRow(int rowIndex) {
    if (aiProviderTypes.size() > rowIndex) {
      aiProviderTypes.remove(rowIndex);
      notifyListeners(rowIndex, aiProviderTypes.size() - 1, -1);
    }
  }

  public void validate() throws ConfigurationException {
    for (AIProviderType environmentType : aiProviderTypes) {
      if (Strings.isEmpty(environmentType.getHostname())) {
        throw new ConfigurationException("Please provide names for all environment types.");
      }
    }
  }
}
