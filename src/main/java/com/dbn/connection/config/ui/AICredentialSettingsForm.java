package com.dbn.connection.config.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.thread.Progress;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SessionId;
import com.dbn.connection.config.AICredentialSettings;
import com.dbn.connection.jdbc.DBNConnection;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class AICredentialSettingsForm extends ConfigurationEditorForm<AICredentialSettings> {
  private JPanel mainPanel;
  private JPanel sshGroupPanel;
  private JLabel credentialNameLabel;
  private JLabel apiPasswordLabel;
  private JTextField apiUsernameField;
  private JButton saveCredential;
  private JPasswordField passwordField;
  private ComboBox apiCredentialNameField;

  public AICredentialSettingsForm(final AICredentialSettings configuration) {
    super(configuration);

    ConnectionId connectionId = configuration.getConnectionId();
    ConnectionHandler currConnection = getConnectionHandler(connectionId);
    if(currConnection != null){

      resetFormChanges();
      registerComponent(mainPanel);
      addEventListeners();
    }
  }

  @NotNull
  @Override
  public JPanel getMainComponent() {
    return mainPanel;
  }

  @Override
  protected ActionListener createActionListener() {
    return e -> System.out.println(apiCredentialNameField.getSelectedItem());
  }

  @Override
  public void applyFormChanges() throws ConfigurationException {
    AICredentialSettings configuration = getConfiguration();
    applyFormChanges(configuration);
    ConnectionId connectionId = configuration.getConnectionId();
    ConnectionHandler currConnection = getConnectionHandler(connectionId);
    try {
      DBNConnection mainConnection = currConnection.getConnection(SessionId.ORACLE_AI);
      Progress.prompt(configuration.getProject(), currConnection, false, "Creating New Credential", "Creating a credential by the name: " + configuration.getCredentialName(), progress -> setupCredential(currConnection, mainConnection, configuration.getCredentialName(), configuration.getApiUsername(), configuration.getApiPassword()));
    } catch (SQLException e){
      System.out.println(e.getMessage());
    }
  }

  public void setupCredential(ConnectionHandler currConnection,
                              DBNConnection mainConnection,
                              String credentialName,
                              String apiName,
                              String apiKey){
//    CredentialProvider credentialAttributes = new PasswordCredentialProvider(credentialName, apiName, apiKey);
//    try{
//      currConnection.getOracleAIInterface().createCredential(mainConnection, credentialAttributes);
//      showInfoDialog(getConfiguration().getProject(), "Credential Creation", "Credential " + getConfiguration().getCredentialName() + " created succesfully");
//
//    } catch (CredentialManagementException e) {
//      throw new RuntimeException(e);
//    }
  }


  public ConnectionHandler getConnectionHandler(ConnectionId id) {
    if (id == null) return null;
    return ConnectionHandler.get(id);
  }

  @Override
  public void applyFormChanges(AICredentialSettings configuration) throws ConfigurationException {
    configuration.setCredentialName(apiCredentialNameField.getSelectedItem().toString());
    configuration.setApiUsername(apiUsernameField.getText());
    configuration.setApiPassword(String.valueOf(passwordField.getPassword()));
  }


  @Override
  public void resetFormChanges() {
    AICredentialSettings configuration = getConfiguration();
    apiCredentialNameField.setSelectedItem(configuration.getCredentialName());
    apiUsernameField.setText(configuration.getApiUsername());
    passwordField.setText(configuration.getApiPassword());
  }




  private void addEventListeners() {
    saveCredential.addActionListener(e -> {
      try {
        applyFormChanges();
      } catch (ConfigurationException ex) {
        ex.printStackTrace();
      }
    });

//    profileButton.addActionListener(e -> {
//      try {
//        applyProfileSetup();
//      } catch (ConfigurationException ex) {
//        ex.printStackTrace();
//      }
//    });
  }
}
