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

package com.dbn.oracleAI.config.ui;

import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.common.util.Dialogs;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.oracleAI.DatabaseAssistantManager;
import com.dbn.oracleAI.ui.ChatBoxState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Main Database Assistant settings dialog
 * Features profiles and credential visualisation and management
 *
 * @author Dan Cioca (Oracle)
 */
public class AssistantSettingsDialog extends DBNDialog<AssistantSettingsForm> {

  private final ConnectionRef connection;

  public AssistantSettingsDialog(ConnectionHandler connection) {
    super(connection.getProject(), getAssistantName(connection) + " Settings", true);
    this.connection = ConnectionRef.of(connection);
    renameAction(getCancelAction(), "Close");

    setDefaultSize(800, 600);
    init();
  }

  private static String getAssistantName(ConnectionHandler connection) {
    Project project = connection.getProject();
    DatabaseAssistantManager assistantManager = DatabaseAssistantManager.getInstance(project);
    ChatBoxState chatBoxState = assistantManager.getChatBoxState(connection.getConnectionId());
    return chatBoxState.getAssistantName();
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getCancelAction(), new HelpAction()};
  }

  private class HelpAction extends AbstractAction {
    private HelpAction() {
      super("Help");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      Dialogs.show(() -> new AssistantPrerequisitesDialog(getConnection()));
    }
  }

  private ConnectionHandler getConnection() {
    return connection.ensure();
  }

  @Override
  protected @NotNull AssistantSettingsForm createForm() {
    return new AssistantSettingsForm(this, getConnection());
  }
}
