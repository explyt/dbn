package com.dbn.oracleAI.config.ui;

import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.ProviderConfiguration;
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

  private final String username;
  ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  // Pass Project object to constructor
  public AICloudSettingsForm(ConnectionHandler connectionHandler) {
    super(false);
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
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return mainPanel;
  }

}
