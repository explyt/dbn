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

package com.dbn.oracleAI;

import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.database.DatabaseFeature;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Editor menu action to invoke AI-Assistant with "Explain SQL" instruction
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
public class ShowSqlExplanationAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) return;

    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (editor == null) return;

    SelectionModel selectionModel = editor.getSelectionModel();
    String selectedText = selectionModel.getSelectedText();
    if (selectedText == null || selectedText.isEmpty()) return;

    VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (file == null) return;

    FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
    ConnectionHandler connection = contextManager.getConnection(file);
    if (connection == null) return;
    if (!DatabaseFeature.AI_ASSISTANT.isSupported(connection)) return;

    new ShowSqlOnEditor(project).processQuery(connection.getConnectionId(), selectedText, editor.getDocument(), true);
  }
}
