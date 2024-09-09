package com.dbn.oracleAI.config.providers.ui;

import com.dbn.common.project.ProjectRef;
import com.dbn.common.ui.table.DBNEditableTableModel;
import com.dbn.common.util.Commons;
import com.dbn.common.util.Strings;
import com.dbn.oracleAI.config.providers.AIProviderCredential;
import com.dbn.oracleAI.config.providers.AIProviderCredentialBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import lombok.Getter;

@Getter
public class AIProviderCredentialTableModel extends DBNEditableTableModel {
  private AIProviderCredentialBundle credentials;
  private final ProjectRef project;

  AIProviderCredentialTableModel(Project project, AIProviderCredentialBundle credentials) {
    this.project = ProjectRef.of(project);
    this.credentials = new AIProviderCredentialBundle(credentials);
  }

  public void setCredentials(AIProviderCredentialBundle credentials) {
    this.credentials = new AIProviderCredentialBundle(credentials);
    notifyListeners(0, credentials.size(), -1);
  }

  @Override
  public int getRowCount() {
    return credentials.size();
  }

  @Override
  public int getColumnCount() {
    return 3;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return columnIndex == 0 ? "Credential Name" :
        columnIndex == 1 ? "User Name" :
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
    AIProviderCredential credential = getAIProviderType(rowIndex);
    return
        columnIndex == 0 ? credential.getName() :
        columnIndex == 1 ? credential.getUser() :
        columnIndex == 2 ? credential.getKey() : null;
  }

  @Override
  public void setValueAt(Object o, int rowIndex, int columnIndex) {
    Object actualValue = getValueAt(rowIndex, columnIndex);
    if (!Commons.match(actualValue, o)) {
      AIProviderCredential credential = credentials.get(rowIndex);
      if (columnIndex == 0) {
        credential.setName((String) o);
      } else if (columnIndex == 1) {
        credential.setUser((String) o);
      } else if (columnIndex == 2) {
        credential.setKey((String) o);
      }

      notifyListeners(rowIndex, rowIndex, columnIndex);
    }
  }

  private AIProviderCredential getAIProviderType(int rowIndex) {
    while (credentials.size() <= rowIndex) {
      credentials.add(new AIProviderCredential());
    }
    return credentials.get(rowIndex);
  }

  @Override
  public void insertRow(int rowIndex) {
    credentials.add(rowIndex, new AIProviderCredential());
    notifyListeners(rowIndex, credentials.size() - 1, -1);
  }

  @Override
  public void removeRow(int rowIndex) {
    if (credentials.size() > rowIndex) {
      credentials.remove(rowIndex);
      notifyListeners(rowIndex, credentials.size() - 1, -1);
    }
  }

  public void validate() throws ConfigurationException {
    for (AIProviderCredential credential : credentials) {
      if (Strings.isEmpty(credential.getName())) {
        throw new ConfigurationException("Please provide names for all credentials.");
      }
    }
  }
}
