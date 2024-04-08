package com.dbn.oracleAI.config.ui;

import com.dbn.connection.ConnectionHandler;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class OracleAISettingsWindow extends JDialog {

  static private final ResourceBundle messages =
      ResourceBundle.getBundle("Messages", Locale.getDefault());

  private JPanel contentPane;
  private JTabbedPane tabbedPane;
  private ConnectionHandler currConnection;

  public OracleAISettingsWindow(ConnectionHandler connection) {
    super(WindowManager.getInstance().getFrame(connection.getProject()), messages.getString(
        "companion.window.title"), true);
    currConnection = connection;
    setContentPane(contentPane);
    setTitle(messages.getString("ai.settings.window.title"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLocationRelativeTo(WindowManager.getInstance().getFrame(connection.getProject()));
    setTabs();
  }

  public void display() {
    setVisible(true);
  }


  private void setTabs() {
    tabbedPane.addTab(messages.getString("ai.settings.window.tab.profiles.title"),new ProfileManagementPanel(currConnection));
    tabbedPane.addTab(messages.getString("ai.settings.window.tab.credentials.title"), new CredentialManagementPanel());
  }
}
