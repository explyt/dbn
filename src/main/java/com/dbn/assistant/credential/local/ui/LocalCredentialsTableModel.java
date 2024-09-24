/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.assistant.credential.local.ui;

import com.dbn.assistant.credential.local.LocalCredential;
import com.dbn.assistant.credential.local.LocalCredentialBundle;
import com.dbn.common.project.ProjectRef;
import com.dbn.common.ui.table.DBNEditableTableModel;
import com.dbn.common.util.Commons;
import com.dbn.common.util.Strings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import lombok.Getter;

@Getter
public class LocalCredentialsTableModel extends DBNEditableTableModel {
  private LocalCredentialBundle credentials;
  private final ProjectRef project;

  LocalCredentialsTableModel(Project project, LocalCredentialBundle credentials) {
    this.project = ProjectRef.of(project);
    this.credentials = new LocalCredentialBundle(credentials);
  }

  public void setCredentials(LocalCredentialBundle credentials) {
    this.credentials = new LocalCredentialBundle(credentials);
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
    LocalCredential credential = getAIProviderType(rowIndex);
    return
        columnIndex == 0 ? credential.getName() :
        columnIndex == 1 ? credential.getUser() :
        columnIndex == 2 ? credential.getKey() : null;
  }

  @Override
  public void setValueAt(Object o, int rowIndex, int columnIndex) {
    Object actualValue = getValueAt(rowIndex, columnIndex);
    if (!Commons.match(actualValue, o)) {
      LocalCredential credential = credentials.get(rowIndex);
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

  private LocalCredential getAIProviderType(int rowIndex) {
    while (credentials.size() <= rowIndex) {
      credentials.add(new LocalCredential());
    }
    return credentials.get(rowIndex);
  }

  @Override
  public void insertRow(int rowIndex) {
    credentials.add(rowIndex, new LocalCredential());
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
    for (LocalCredential credential : credentials) {
      if (Strings.isEmpty(credential.getName())) {
        throw new ConfigurationException("Please provide names for all credentials.");
      }
    }
  }
}
