package com.dbn.oracleAI.config.ui;

import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.Profile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Represents a panel for managing AI credentials within the application.
 * This panel allows users to view, edit, and delete AI credentials associated with a specific connection.
 */
public class CredentialManagementPanel extends JPanel {

  static private final ResourceBundle messages =
      ResourceBundle.getBundle("Messages", Locale.getDefault());

  private JPanel mainPane;
  private JList<String> credentialList;
  private JPanel displayInfo;
  private JLabel profilesLabel;
  private JButton deleteButton;
  private JButton button1;
  private JButton editButton;
  private final AICredentialService credentialSvc;
  private Map<String, CredentialProvider> credentialsProvidersMap;

  /**
   * Initializes a new instance of the CredentialManagementPanel with a specified connection.
   * This constructor sets up the UI components and fetches the credentials for the given connection.
   *
   * @param connection The ConnectionHandler associated with this panel, used to fetch and manage credentials.
   */
  public CredentialManagementPanel(ConnectionHandler connection) {
    this.credentialSvc = connection.getProject().getService(DatabaseOracleAIManager.class).getCredentialService();
    fetchCredentialsProviders();

    this.add(mainPane);
  }

  /**
   * Fetches the list of credential providers from the AI credential service and updates the UI.
   * This method asynchronously retrieves the credentials, updating the credential list and display information.
   */
  private void fetchCredentialsProviders() {
    credentialSvc.listCredentialsDetailed().thenAccept(credentialProviderList -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        this.credentialsProvidersMap = credentialProviderList.stream().collect(Collectors.toMap(CredentialProvider::getCredentialName, cred -> cred));
        credentialList.setListData(credentialsProvidersMap.values().stream().map(CredentialProvider::getCredentialName).toArray(String[]::new));
        credentialList.addListSelectionListener((e) -> {
          CredentialProvider selectedCredentialProvider = credentialsProvidersMap.get(credentialList.getSelectedValue());
          displayInfo.removeAll();
          panelTemplate(selectedCredentialProvider.getCredentialName(), selectedCredentialProvider.getUsername());
          profilesLabel.setText(selectedCredentialProvider.getProfiles().stream().map(Profile::getProfileName).collect(Collectors.joining(", ")));
        });
      });
    });
  }

  /**
   * Updates the display information panel based on a selected credential.
   * This method dynamically creates and displays UI components such as labels and text fields
   * to show detailed information for the selected credential, including its name and associated username.
   *
   * @param credentialName The name of the credential to display information for.
   * @param username The username associated with the selected credential.
   */
  public void panelTemplate(String credentialName, String username) {

    GridBagConstraints constraints = new GridBagConstraints();

    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.weightx = 1.0;
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.insets = JBUI.insets(2);


    displayInfo.add(new JLabel(messages.getString("ai.settings.credentials.info.credential_name")), constraints);
    constraints.gridx = 1;
    JTextField credentialNameField = new JTextField(credentialName);
    displayInfo.add(credentialNameField, constraints);


    constraints.gridx = 0;


    displayInfo.add(new JLabel(messages.getString("ai.settings.credentials.info.username")), constraints);
    constraints.gridx = 1;
    JTextField userNameField = new JTextField(username);
    displayInfo.add(userNameField, constraints);

    displayInfo.revalidate();
    displayInfo.repaint();
  }


}
