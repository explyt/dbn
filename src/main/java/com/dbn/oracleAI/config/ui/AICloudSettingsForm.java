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

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.ProviderConfiguration;
import com.dbn.oracleAI.service.DatabaseService;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.dbn.nls.NlsResources.txt;

public class AICloudSettingsForm extends DialogWrapper {

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
  private JLabel linkLabel;
  private final String SELECT_AI_DOCS = "https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/doc/sql-generation-ai-autonomous.html";

  private final String username;
  private final DatabaseService databaseSvc;
  private final ConnectionHandler connectionHandler;

  // Pass Project object to constructor
  public AICloudSettingsForm(ConnectionHandler connectionHandler) {
    super(true);
    this.databaseSvc = DatabaseService.getInstance(connectionHandler);

    this.connectionHandler = connectionHandler;
    this.username = connectionHandler.getUserName();
    initializeWindow();
    setTitle("Select AI - Help");
    init();
    pack();
    setResizable(false);
  }

  private void initializeWindow() {
    providerComboBox.addItem(ProviderType.OPENAI);
    providerComboBox.addItem(ProviderType.COHERE);
    providerComboBox.addItem(ProviderType.OCI);

    linkLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (Desktop.isDesktopSupported()) {
          try {
            Desktop.getDesktop().browse(new URI(SELECT_AI_DOCS));
          } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
          }
        }
      }
    });
    linkLabel.setText("SelectAI Docs");
    linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    linkLabel.setForeground(JBColor.BLUE);

    grantTextField.setText(txt("permissions3.message", username));

    grantTextArea.setText(txt("permissions4.message", username));

    networkAllow.setText(txt("permissions5.message"));

    aclTextArea.setText(txt("permissions6.message", ProviderConfiguration.getAccessPoint((ProviderType) providerComboBox.getSelectedItem()), username));

    providerComboBox.addActionListener(e -> {
      aclTextArea.setText(txt("permissions6.message", ProviderConfiguration.getAccessPoint((ProviderType) providerComboBox.getSelectedItem()), username));
    });

    copyPrivilegeButton.addActionListener(e -> copyTextToClipboard(grantTextArea.getText()));
    copyACLButton.addActionListener(e -> copyTextToClipboard(aclTextArea.getText()));


    applyPrivilegeButton.setToolTipText(txt("privilege.apply.disabled"));
    applyACLButton.setToolTipText(txt("privilege.apply.disabled"));

    applyPrivilegeButton.addActionListener(e -> grantPrivileges(username));
    applyACLButton.addActionListener(e -> grantACLRights(aclTextArea.getText()));

    isUserAdmin();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return mainPanel;
  }

  @Override
  protected JComponent createSouthPanel() {
    JComponent wizard = super.createSouthPanel();
    MatteBorder topBorder = new MatteBorder(1, 0, 0, 0, new Color(43, 45, 48));
    EmptyBorder emptyBorder = new EmptyBorder(7, 0, 0, 0);
    Border compoundBorder = new CompoundBorder(topBorder, emptyBorder);
    wizard.setBorder(compoundBorder);
    return wizard;
  }

  @Override
  protected Action @NotNull [] createActions() {
    super.setCancelButtonText(txt("profiles.mgnt.buttons.close.text"));

    return new Action[]{super.getCancelAction()};
  }

  private void copyTextToClipboard(String text) {
    StringSelection selection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, null);
  }

  private void grantACLRights(String command) {
    databaseSvc.grantACLRights(command)
        .thenAccept(a -> {
          SwingUtilities.invokeLater(() -> {
            Messages.showInfoDialog(connectionHandler.getProject(), txt("privileges.granted.title"), txt("privileges.granted.message"));
          });
        })
        .exceptionally(e -> {
          SwingUtilities.invokeLater(() -> {
            Messages.showErrorDialog(connectionHandler.getProject(), txt("privileges.not_granted.title"), txt("privileges.not_granted.message") + e.getMessage());
          });
          return null;
        });


  }

  private void grantPrivileges(String username) {
    databaseSvc.grantPrivilege(username)
        .thenAccept(a -> {
          SwingUtilities.invokeLater(() -> {
            Messages.showInfoDialog(connectionHandler.getProject(), txt("privileges.granted.title"), txt("privileges.granted.message"));
          });
        })
        .exceptionally(e -> {
          SwingUtilities.invokeLater(() -> {
            Messages.showErrorDialog(connectionHandler.getProject(), txt("privileges.not_granted.title"), txt("privileges.not_granted.message") + e.getMessage());
          });
          return null;
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
