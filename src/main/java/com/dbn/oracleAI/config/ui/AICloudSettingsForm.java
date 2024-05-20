package com.dbn.oracleAI.config.ui;

import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
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
  private JButton copyButton;
  private JButton applyButton;
  private JLabel grantTextField;

  private String userName;
  private ProviderType hostname = ProviderType.OPENAI;
  ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  // Pass Project object to constructor
  public AICloudSettingsForm(ConnectionHandler connectionHandler) {
    super(false);
    this.userName = connectionHandler.getUserName();

    initializeWindow();
    init();
    pack();
  }

  private void initializeWindow() {
    providerComboBox.addItem(ProviderType.OPENAI);
    providerComboBox.addItem(ProviderType.COHERE);

    intro.setText(messages.getString("permissions1.message"));
    intro2.setText(messages.getString("permissions2.message"));

    grantTextField.setText(String.format(messages.getString("permissions3.message"), userName));

    grantTextArea.setText(String.format(messages.getString("permissions4.message"), userName, userName));

    networkAllow.setText(messages.getString("permissions5.message"));

    aclTextArea.setText(String.format(messages.getString("permissions6.message"), hostname.getHostname(), userName));

    providerComboBox.addActionListener(e -> {
      hostname = (ProviderType) providerComboBox.getSelectedItem();
      aclTextArea.setText(String.format(messages.getString("permissions6.message"), hostname.getHostname(), userName));
    });
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return mainPanel;
  }

}
