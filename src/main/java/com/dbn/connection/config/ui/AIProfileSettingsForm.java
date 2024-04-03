package com.dbn.connection.config.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.thread.Progress;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SessionId;
import com.dbn.connection.config.AIProfileSettings;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import static com.dbn.common.util.Messages.showInfoDialog;


public class AIProfileSettingsForm extends ConfigurationEditorForm<com.dbn.connection.config.AIProfileSettings> {
  private JPanel mainPanel;

  private JPanel sshGroupPanel;
  private JTextField apiCredentialNameField;
  private JTextField profileNameField;
  private JComboBox<String> credentialNameBox;
  private JComboBox<String> providerBox;
  private JComboBox<String> modelBox;
  private JButton profileButton;

  public AIProfileSettingsForm(final com.dbn.connection.config.AIProfileSettings configuration) {
    super(configuration);

    ConnectionId connectionId = configuration.getConnectionId();
    ConnectionHandler currConnection = getConnectionHandler(connectionId);
    if(currConnection != null){

      resetFormChanges();
      loadBoxes();
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
    return e -> System.out.println(apiCredentialNameField.getText());
  }

  @Override
  public void applyFormChanges() throws ConfigurationException {
    com.dbn.connection.config.AIProfileSettings configuration = getConfiguration();
    applyFormChanges(configuration);
    ConnectionId connectionId = configuration.getConnectionId();
    ConnectionHandler currConnection = getConnectionHandler(connectionId);
    try {
      DBNConnection mainConnection = currConnection.getConnection(SessionId.ORACLE_AI);
      Progress.prompt(configuration.getProject(), currConnection, false, "Creating New Profile", "Creating a profile by the name: " + configuration.getProfileName(), progress -> setupProfile(currConnection, mainConnection, configuration.getProfileName(), configuration.getProvider(), configuration.getCredentialBoxName()));
    } catch (SQLException e){
      System.out.println(e.getMessage());
    }
    }

    public void setupProfile(ConnectionHandler currConnection,
                                DBNConnection mainConnection,
                                String profileName,
                                String provider,
                                String credentialName){
    assert provider != null: "provider cannot be null";

    Profile profileAttributes = Profile.builder()
          .profileName(profileName)
          .credentialName(credentialName)
          .provider(ProviderType.valueOf(provider))
          .build();
    try {
      currConnection.getOracleAIInterface().createProfile(mainConnection, profileAttributes);
      showInfoDialog(getConfiguration().getProject(), "Profile Creation", "Credential " + getConfiguration().getProfileName() + " created succesfully");
    }
    catch (ProfileManagementException e) {
      throw new RuntimeException(e);
    }
    }


  public ConnectionHandler getConnectionHandler(ConnectionId id) {
    if (id == null) return null;
    return ConnectionHandler.get(id);
  }

  @Override
  public void applyFormChanges(com.dbn.connection.config.AIProfileSettings configuration) throws ConfigurationException {
    configuration.setProfileName(profileNameField.getText());
//    configuration.setCredentialBoxName(credentialNameBox.getSelectedItem().toString());
//    configuration.setProvider(String.valueOf(providerBox.getSelectedItem().toString()));
//    configuration.setModel(modelBox.getSelectedItem().toString());
  }

  @Override
  public void resetFormChanges() {
    AIProfileSettings configuration = getConfiguration();
    profileNameField.setText(configuration.getProfileName());
    credentialNameBox.setSelectedItem(configuration.getCredentialBoxName());
    providerBox.setSelectedItem(configuration.getProvider());
    modelBox.setSelectedItem(configuration.getModel());
  }

  public void loadCredentials() {
    AIProfileSettings configuration = getConfiguration();
    ConnectionId connectionId = configuration.getConnectionId();
    ConnectionHandler currConnection = getConnectionHandler(connectionId);
    ApplicationManager.getApplication().executeOnPooledThread(()->{

    try {
      DBNConnection mainConnection = currConnection.getConnection(SessionId.ORACLE_AI);
      List<CredentialProvider> credentials = currConnection.getOracleAIInterface().listCredentials(mainConnection);

      for(CredentialProvider credential : credentials){
        credentialNameBox.addItem(credential.getCredentialName());
      }
    } catch (DatabaseOperationException e) {
      throw new RuntimeException(e);
    } catch (SQLException e){
      System.out.println(e);
    }

    });
  }

  public void loadBoxes() {
    providerBox.addItem("cohere");
    providerBox.addItem("openai");
    providerBox.setSelectedItem("cohere");
    modelBox.addItem("3.5");
    modelBox.addItem("4");
    modelBox.setSelectedItem("3.5");
    loadCredentials();
  }
  private void addEventListeners() {
    profileButton.addActionListener(e -> {
      try {
        applyFormChanges();
      } catch (ConfigurationException ex) {
        ex.printStackTrace();
      }
    });

  }
}
