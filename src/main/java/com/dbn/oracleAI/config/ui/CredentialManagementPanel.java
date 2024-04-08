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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A panel for managing AI credentials within the application. It provides functionalities
 * to view, edit, and delete AI credentials associated with a specific connection. This UI component
 * is part of the Oracle AI integration module, allowing users to efficiently manage their AI service
 * credentials directly within the IDE.
 *
 * The panel is dynamically populated with credential information retrieved from the AI credential service,
 * and it utilizes the {@link ConnectionHandler} to fetch and manage credentials for a specific project connection.
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
   * Initializes a new instance of the CredentialManagementPanel for managing AI credentials,
   * setting up UI components and fetching credentials for the given connection.
   *
   * @param connection The ConnectionHandler associated with this panel. It is used to fetch
   *                   and manage credentials related to the project's Oracle AI integration.
   */
  public CredentialManagementPanel(ConnectionHandler connection) {
    this.credentialSvc = connection.getProject().getService(DatabaseOracleAIManager.class).getCredentialService();
    initializeUI();
    fetchCredentialsProviders();

    this.add(mainPane);
  }

  /**
   * Initializes UI components of the panel, setting up list selection listeners for credential selection,
   * and configuring the appearance of the list and its cells. This method is responsible for the initial
   * UI setup and layout of the credential management panel.
   */
  private void initializeUI() {
    credentialList.addListSelectionListener((e) -> {
      CredentialProvider selectedCredentialProvider = credentialsProvidersMap.get(credentialList.getSelectedValue());
      displayInfo.removeAll();
      panelTemplate(selectedCredentialProvider.getCredentialName(), selectedCredentialProvider.getUsername());
      profilesLabel.setText(selectedCredentialProvider.getProfiles().stream().map(Profile::getProfileName).collect(Collectors.joining(", ")));
    });
    credentialList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return c;
      }
    });
  }
  /**
   * Asynchronously fetches the list of credential providers from the AI credential service and updates
   * the UI components accordingly. This method retrieves the credentials, updating the credential list
   * and the display information panel based on the available credentials for the connected project.
   */
  private void fetchCredentialsProviders() {
    credentialSvc.listCredentialsDetailed().thenAccept(credentialProviderList -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        credentialsProvidersMap = credentialProviderList.stream()
            .collect(Collectors.toMap(CredentialProvider::getCredentialName, Function.identity()));
        updateCredentialList();

      });
    });
  }

  /**
   * Updates the credential list UI component with the names of the available credential providers.
   * This method is called after the credential providers have been fetched to refresh the displayed list.
   */
  private void updateCredentialList() {
    credentialList.setListData(credentialsProvidersMap.values().stream().map(CredentialProvider::getCredentialName).toArray(String[]::new));
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
