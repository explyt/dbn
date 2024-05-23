package com.dbn.oracleAI.config.ui;

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.DatabaseServiceImpl;
import com.dbn.oracleAI.config.ProviderConfiguration;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
  private JLabel linkLabel;
  private final String SELECT_AI_DOCS = "https://docs.oracle.com/en-us/iaas/autonomous-database-serverless/doc/sql-generation-ai-autonomous.html";

  private final String username;
  private final DatabaseServiceImpl manager;
  private final ConnectionHandler connectionHandler;
  ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  // Pass Project object to constructor
  public AICloudSettingsForm(ConnectionHandler connectionHandler) {
    super(true);
    this.manager = (DatabaseServiceImpl) connectionHandler.getProject().getService(DatabaseOracleAIManager.class).getDatabaseService();
    ;
    this.connectionHandler = connectionHandler;
    this.username = connectionHandler.getUserName();
    initializeWindow();

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
    linkLabel.setForeground(JBColor.CYAN);

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
    super.setCancelButtonText(messages.getString("profiles.mgnt.buttons.close.text"));

    return new Action[]{super.getCancelAction()};
  }

  private void copyTextToClipboard(String text) {
    StringSelection selection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(selection, null);
  }

  private void grantACLRights(String command) {
    manager.grantACLRights(command)
        .thenAccept(a -> {
          SwingUtilities.invokeLater(() -> {
            Messages.showInfoDialog(connectionHandler.getProject(), messages.getString("privileges.granted.title"), messages.getString("privileges.granted.message"));
          });
        })
        .exceptionally(e -> {
          SwingUtilities.invokeLater(() -> {
            Messages.showErrorDialog(connectionHandler.getProject(), messages.getString("privileges.not_granted.title"), messages.getString("privileges.not_granted.message") + e.getMessage());
          });
          return null;
        });


  }

  private void grantPrivileges(String username) {
    manager.grantPrivilege(username)
        .thenAccept(a -> {
          SwingUtilities.invokeLater(() -> {
            Messages.showInfoDialog(connectionHandler.getProject(), messages.getString("privileges.granted.title"), messages.getString("privileges.granted.message"));
          });
        })
        .exceptionally(e -> {
          SwingUtilities.invokeLater(() -> {
            Messages.showErrorDialog(connectionHandler.getProject(), messages.getString("privileges.not_granted.title"), messages.getString("privileges.not_granted.message") + e.getMessage());
          });
          return null;
        });
  }

  private void isUserAdmin() {
    manager.isUserAdmin()
        .thenAccept(a -> {
          SwingUtilities.invokeLater(() -> {
            applyACLButton.setEnabled(true);
            applyPrivilegeButton.setEnabled(true);
          });
        });
  }
}
