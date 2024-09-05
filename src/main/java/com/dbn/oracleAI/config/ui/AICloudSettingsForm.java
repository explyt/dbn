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

import com.dbn.common.thread.Progress;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dbn.diagnostics.Diagnostics;
import com.dbn.oracleAI.config.ProviderConfiguration;
import com.dbn.oracleAI.service.DatabaseService;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HyperlinkLabel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import static com.dbn.common.Priority.HIGH;
import static com.dbn.common.util.Conditional.when;

/**
 * Database Assistant prerequisites information form
 * Explains the necessary grants and access rights for Select AI.
 * Also provisions actions to grant such privileges
 * TODO FEATURE proposal: "Grant for colleague" allowing the user to select another user for the grant operation
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 * @author Dan Cioca (dan.cioca@oracle.com)
 */
public class AICloudSettingsForm extends DBNFormBase {

  private JPanel mainPanel;
  private JLabel intro;
  private JLabel networkAllow;
  private JComboBox<ProviderType> providerComboBox;
  private JTextArea aclTextArea;
  private JTextArea grantTextArea;
  private JLabel grantTextField;
  private JButton copyACLButton;
  private JButton applyACLButton;
  private JButton copyPrivilegeButton;
  private JButton applyPrivilegeButton;
  private JPanel headerPanel;
  private HyperlinkLabel docuLink;
  private final String SELECT_AI_DOCS = "https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/doc/sql-generation-ai-autonomous.html";

  private final ConnectionRef connection;
  private final DatabaseService databaseSvc;

  // Pass Project object to constructor
  public AICloudSettingsForm(AssistantPrerequisitesDialog dialog) {
    super(dialog);

    ConnectionHandler connection = dialog.getConnection();
    this.connection = ConnectionRef.of(connection);
    this.databaseSvc = DatabaseService.getInstance(connection);

    initHeaderPanel();
    initializeWindow();
  }

  private ConnectionHandler getConnection() {
    return connection.ensure();
  }

  private void initHeaderPanel() {
    ConnectionHandler connection = getConnection();
    DBNHeaderForm headerForm = new DBNHeaderForm(this, connection);
    headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }

  private void initializeWindow() {
    providerComboBox.addItem(ProviderType.OPENAI);
    providerComboBox.addItem(ProviderType.COHERE);
    providerComboBox.addItem(ProviderType.OCI);

    docuLink.addHyperlinkListener(e -> when(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED, () -> BrowserUtil.browse(SELECT_AI_DOCS)));
    docuLink.setHyperlinkText("Select AI Docs");

    String userName = getConnection().getUserName();
    grantTextField.setText(txt("permissions3.message", userName));
    grantTextArea.setText(txt("permissions4.message", userName));

    networkAllow.setText(txt("permissions5.message"));
    aclTextArea.setText(txt("permissions6.message", getAccessPoint(), userName));

    providerComboBox.addActionListener(e -> aclTextArea.setText(txt("permissions6.message", getAccessPoint(), userName)));

    copyPrivilegeButton.addActionListener(e -> copyTextToClipboard(grantTextArea.getText()));
    copyACLButton.addActionListener(e -> copyTextToClipboard(aclTextArea.getText()));

    applyPrivilegeButton.setToolTipText(txt("privilege.apply.disabled"));
    applyACLButton.setToolTipText(txt("privilege.apply.disabled"));

    applyPrivilegeButton.addActionListener(e -> grantExecutionPrivileges());
    applyACLButton.addActionListener(e -> grantNetworkAccess());

    isUserAdmin();
  }

  private String getAccessPoint() {
    ProviderType selectedProvider = getSelectedProvider();
    return selectedProvider == null ? "" : ProviderConfiguration.getAccessPoint(selectedProvider);
  }

  @Nullable
  private ProviderType getSelectedProvider() {
    return (ProviderType) providerComboBox.getSelectedItem();
  }

  private void copyTextToClipboard(String text) {
    StringSelection selection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, null);
  }

  // TODO move in dedicated AssistantPrerequisitesManager
  private void grantNetworkAccess() {
    String command = aclTextArea.getText();
    ConnectionHandler connection = getConnection();
    Project project = connection.getProject();

    String host = getAccessPoint();
    String user = connection.getUserName();
    String title = txt("prc.assistant.title.GrantingAccess");
    String message = txt("prc.assistant.message.GrantingNetworkAccess", host, user);

    Progress.modal(project, connection, false, title, message, progress -> {
      try {
        DatabaseInterfaceInvoker.execute(HIGH, title, message, project, connection.getConnectionId(),
                c -> connection.getAssistantInterface().grantACLRights(c, command));

        showInfoDialog(txt("msg.assistant.title.AccessGranted"), txt("msg.assistant.info.NetworkAccessGranted", host, user));
      } catch (Throwable e) {
        Diagnostics.conditionallyLog(e);
        showErrorDialog(txt("msg.assistant.title.AccessGrantFailed"), txt("msg.assistant.error.NetworkAccessGrantFailed", host, user, e.getMessage()));
      }
    });
  }
  // TODO move in dedicated AssistantPrerequisitesManager
  private void grantExecutionPrivileges() {
    ConnectionHandler connection = getConnection();
    Project project = connection.getProject();

    String user = connection.getUserName();
    String title = txt("prc.assistant.title.GrantingPrivileges");
    String message = txt("prc.assistant.message.GrantingExecutionPrivileges", user);

    Progress.modal(project, connection, false, title, message, progress -> {
      try {
        DatabaseInterfaceInvoker.execute(HIGH, title, message, project, connection.getConnectionId(),
                c -> connection.getAssistantInterface().grantPrivilege(c, user));

        showInfoDialog(txt("msg.assistant.title.PrivilegesGranted"), txt("msg.assistant.info.ExecutionPrivilegesGranted", user));
      } catch (Throwable e) {
        Diagnostics.conditionallyLog(e);
        showErrorDialog(txt("msg.assistant.title.PrivilegesGrantFailed"), txt("msg.assistant.error.ExecutionPrivilegesGrantFailed", user, e.getMessage()));
      }
    });
  }

  private void isUserAdmin() {
    databaseSvc.isUserAdmin()
        .thenAccept(a -> {
          SwingUtilities.invokeLater(() -> {
            applyACLButton.setEnabled(true);
            applyPrivilegeButton.setEnabled(true);

            applyPrivilegeButton.setToolTipText(txt("privilege.apply.enabled"));
            applyACLButton.setToolTipText(txt("privilege.apply.enabled"));
          });
        });
  }
}
