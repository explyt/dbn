package com.dbn.connection.config.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.config.AICloudSettings;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

public class AICloudSettingsForm extends ConfigurationEditorForm<AICloudSettings> {

  private JPanel mainPanel;
  private JLabel intro;
  private JLabel networkAllow;
  private JComboBox providerComboBox;
  private JTextArea aclTextArea;
  private JTextArea grantTextArea;
  private JLabel intro2;
  private JButton copyButton;
  private JButton applyButton;
  private JLabel grantTextField;

  // Variable to hold the user name, replacing the hard-coded "SCOTT"
  private String userName;
  private ProviderType hostname = ProviderType.OPENAI;

  public AICloudSettingsForm(final AICloudSettings aiCloudSettings) {
    super(aiCloudSettings);
    if(ConnectionHandler.get(getConfiguration().getConnectionId())!=null) {
      this.userName = ConnectionHandler.get(getConfiguration().getConnectionId()).getUserName();
    }
    providerComboBox.addItem(ProviderType.OPENAI);
    providerComboBox.addItem(ProviderType.COHERE);

    ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());





//    intro.setText(messages.getString("permissions1.message"));
//    intro2.setText(messages.getString("permissions2.message"));
//
//    grantTextField.setText( String.format(messages.getString("permissions3.message"), userName) );
//
//    grantTextArea.setText( String.format(messages.getString("permissions4.message"), userName, userName) );
//
//    networkAllow.setText(messages.getString("permissions5.message"));
//
//    aclTextArea.setText( String.format(messages.getString("permissions6.message"), hostname.getAction().toLowerCase(), userName) );
//
//    providerComboBox.addActionListener (e -> {
//      hostname = (ProviderType) providerComboBox.getSelectedItem();
//      aclTextArea.setText( String.format(messages.getString("permissions6.message"), hostname.getAction().toLowerCase(), userName) );
//
//    });
  }

  @Override
  public void applyFormChanges() throws ConfigurationException {

  }

  @Override
  public void resetFormChanges() {

  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }
}
