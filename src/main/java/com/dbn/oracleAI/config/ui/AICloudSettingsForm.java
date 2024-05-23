package com.dbn.oracleAI.config.ui;

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.SessionId;
import com.dbn.oracleAI.config.ProviderConfiguration;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class AICloudSettingsForm extends DialogWrapper {

  private JPanel mainPanel;
  private JLabel intro;
  private JLabel networkAllow;
  private JComboBox<ProviderType> providerComboBox;
  private JTextArea aclTextArea;
  private JTextArea grantTextArea;
  private JLabel intro2;
  private JLabel grantTextField;
  private JButton copyACLButton;
  private JButton applyACLButton;
  private JButton copyPrivilegeButton;
  private JButton applyPrivilegeButton;

  private final String username;
  private final ConnectionHandler connectionHandler;
  ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  // Pass Project object to constructor
  public AICloudSettingsForm(ConnectionHandler connectionHandler) {
    super(true);
    this.connectionHandler = connectionHandler;
    this.username = connectionHandler.getUserName();
    initializeWindow();
    init();
    pack();
  }

  private void initializeWindow() {
    providerComboBox.addItem(ProviderType.OPENAI);
    providerComboBox.addItem(ProviderType.COHERE);
    providerComboBox.addItem(ProviderType.OCI);

    intro.setText(messages.getString("permissions1.message"));
    intro2.setText(messages.getString("permissions2.message"));

    grantTextField.setText(String.format(messages.getString("permissions3.message"), username));

    grantTextArea.setText(String.format(messages.getString("permissions4.message"), username, username));

    networkAllow.setText(messages.getString("permissions5.message"));

    aclTextArea.setText(String.format(messages.getString("permissions6.message"), ProviderConfiguration.getAccessPoint((ProviderType) providerComboBox.getSelectedItem()), username));

    providerComboBox.addActionListener(e -> {
      aclTextArea.setText(String.format(messages.getString("permissions6.message"), ProviderConfiguration.getAccessPoint((ProviderType) providerComboBox.getSelectedItem()), username));
    });

    copyPrivilegeButton.addActionListener(e -> copyTextToClipboard(grantTextArea.getText()));
    copyACLButton.addActionListener(e -> copyTextToClipboard(aclTextArea.getText()));

    applyPrivilegeButton.addActionListener(e -> grantPrivileges(username));
    applyACLButton.addActionListener(e -> grantACLRights(aclTextArea.getText()));
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return mainPanel;
  }


  @Override
  protected Action @NotNull [] createActions() {
    super.setCancelButtonText("Close");

    return new Action[]{super.getCancelAction()};
  }

  private void copyTextToClipboard(String text) {
    StringSelection selection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, null);
  }

  private void grantACLRights(String command) {
    try {
      connectionHandler.getOracleAIInterface().grantACLRights(connectionHandler.getConnection(SessionId.ORACLE_AI), command);
      Messages.showInfoDialog(connectionHandler.getProject(), "Granting Privileges Succeeded", "You got the privileges!");
    } catch (SQLException e) {
      Messages.showErrorDialog(connectionHandler.getProject(), "Granting Privileges Failed", "You failed to grant privileges, a user with enough right should execute this.\n" + e.getMessage());
    }
  }

  private void grantPrivileges(String username) {
    try {
      connectionHandler.getOracleAIInterface().grantPrivilege(connectionHandler.getConnection(SessionId.ORACLE_AI), username);
      Messages.showInfoDialog(connectionHandler.getProject(), "Granting Privileges Succeeded", "You got the privileges!");
    } catch (SQLException e) {
      Messages.showErrorDialog(connectionHandler.getProject(), "Granting Privileges Failed", "You failed to grant privileges, a user with enough right should execute this.\n" + e.getMessage());
    }
  }
}
