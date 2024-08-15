package com.dbn.oracleAI.config.ui;

import com.dbn.connection.ConnectionHandler;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;

import static com.dbn.nls.NlsResources.txt;

public class OracleAISettingsWindow extends JDialog {

  private JPanel settingsWindowContentPane;
  private JTabbedPane settingsTabbedPane;
  private JPanel settingsWindowBottomPanel;
  private JButton settingsCloseButton;
  private JButton helpButton;
  private final ConnectionHandler currConnection;

  public OracleAISettingsWindow(ConnectionHandler connection) {
    super(WindowManager.getInstance().getFrame(connection.getProject()), txt("ai.settings.window.title"), true);
    currConnection = connection;
    setContentPane(settingsWindowContentPane);
    setTitle(txt("ai.settings.window.title"));
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setupBottomPanel();
    pack();
    setLocationRelativeTo(WindowManager.getInstance().getFrame(connection.getProject()));
    setTabs();
  }

  private void setupBottomPanel() {
    settingsWindowBottomPanel.setLayout(new BorderLayout());

    settingsCloseButton.addActionListener(e -> dispose());

    helpButton.addActionListener(e -> {
      AICloudSettingsForm helpDialog = new AICloudSettingsForm(currConnection);
      helpDialog.showAndGet();
    });

    settingsWindowBottomPanel.add(helpButton, BorderLayout.WEST);
    settingsWindowBottomPanel.add(settingsCloseButton, BorderLayout.EAST);
  }

  private void setTabs() {
    settingsTabbedPane.addTab(txt("ai.settings.window.tab.profiles.title"), new ProfileManagementPanel(currConnection));
    settingsTabbedPane.addTab(txt("ai.settings.window.tab.credentials.title"), new CredentialManagementPanel(currConnection));
  }

  public void display() {
    setVisible(true);
  }
}
