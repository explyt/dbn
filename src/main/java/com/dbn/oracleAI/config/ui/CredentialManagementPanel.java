package com.dbn.oracleAI.config.ui;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.ui.ActivityNotifier;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * A panel for managing AI credentials within the application, offering functionalities
 * to view, edit, and delete AI credentials associated with a specific connection. This component
 * is part of the Oracle AI integration module, enabling users to manage their AI service
 * credentials directly within the IDE environment.
 * <p>
 * The panel dynamically populates with credential information retrieved from the AI credential service,
 * leveraging the {@link ConnectionHandler} to fetch and manage credentials for a given project connection.
 */
public class CredentialManagementPanel extends JPanel {

  static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  private final ActivityNotifier activityNotifier;

  private JPanel mainPane;
  private JList<Credential> credentialList;
  private JPanel displayInfo;
  private JLabel profilesLabel;
  private JButton deleteButton;
  private JButton addButton;
  private JButton editButton;
  private JLabel profilesLabelTitle;
  private JList<String> usedByList;
  private JScrollPane usedByScrollPane;
  private JProgressBar progressBar1;
  private final AICredentialService credentialSvc;
  private final AIProfileService profileSvc;
  private final ConnectionRef connection;
  private final Project curProject;

  /**
   * Keeps a mapping of profile names that used a specific credential name
   * (Assuming that credential names are unique within the DB)
   */
  private final Map<Credential, List<String>> credentialNameToProfileNameMap = new HashMap<>();

  /**
   * Initializes a new instance of the CredentialManagementPanel for managing AI credentials,
   * setting up UI components and fetching credentials for the given connection.
   *
   * @param connection The ConnectionHandler associated with this panel, used for fetching
   *                   and managing credentials related to the project's Oracle AI integration.
   */
  public CredentialManagementPanel(ConnectionHandler connection) {
    if (connection == null || connection.ref().get() == null) {
      throw new IllegalArgumentException("connection cannot be null");
    }
    this.credentialSvc = connection.getProject().getService(DatabaseOracleAIManager.class).getCredentialService();
    this.profileSvc = connection.getProject().getService(DatabaseOracleAIManager.class).getProfileService();

    this.connection = connection.ref();
    this.curProject = connection.getProject();

    GridConstraints constraints = new GridConstraints();
    constraints.setRow(0);
    constraints.setColumn(0);
    constraints.setColSpan(3);
    constraints.setRowSpan(1);
    constraints.setFill(GridConstraints.FILL_HORIZONTAL);

    this.activityNotifier = new ActivityNotifier();

    mainPane.add(this.activityNotifier, constraints);


    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(mainPane);

    updateCredentialList();

  }

  /**
   * Initializes UI components of the panel, including setting up list selection listeners for credential selection,
   * configuring the appearance of the list and its cells, and initializing action listeners for add and delete buttons.
   * This method is responsible for the initial UI setup and layout of the credential management panel.
   */
  private void initializeUI() {
    editButton.setIcon(Icons.ACTION_EDIT);
    addButton.setIcon(Icons.ACTION_ADD);
    deleteButton.setIcon(Icons.ACTION_DELETE);

    credentialList.setListData(credentialNameToProfileNameMap.keySet().toArray(new Credential[]{}));
    credentialList.setSelectedIndex(0);

    editButton.setEnabled(true);
    addButton.setEnabled(true);
    deleteButton.setEnabled(true);

    // Initializes addButton with its action listener for creating new credential
    addButton.addActionListener((e) -> {
      CredentialCreationCallback callback = this::updateCredentialList;
      CredentialCreationWindow win = new CredentialCreationWindow(curProject, credentialSvc, null, callback);
      win.setExistingCredentialNames(
          credentialNameToProfileNameMap.keySet().stream().map(c -> c.getCredentialName()).collect(Collectors.toList()));
      win.showAndGet();

    });

    editButton.addActionListener((e) -> {
      CredentialCreationCallback callback = this::updateCredentialList;
      CredentialCreationWindow win = new CredentialCreationWindow(curProject, credentialSvc, credentialList.getSelectedValue(), callback);
      win.setExistingCredentialNames(credentialNameToProfileNameMap.keySet().stream().map(c -> c.getCredentialName()).collect(Collectors.toList()));
      win.showAndGet();
    });
    // Initializes deleteButton with its action listener for deleting selected credentials
    deleteButton.addActionListener(e -> {
      StringBuilder detailedMessage = new StringBuilder(messages.getString("ai.settings.credential.deletion.message.prefix"));
      detailedMessage.append(' ');
      detailedMessage.append(credentialList.getSelectedValue().getCredentialName());
      List<String> uses = credentialNameToProfileNameMap.get(credentialList.getSelectedValue());
      if (uses.size() > 0) {
        detailedMessage.append('\n');
        detailedMessage.append(messages.getString("ai.settings.credential.deletion.message.warning"));
        uses.forEach(c -> {
          detailedMessage.append(c);
          detailedMessage.append(", ");
        });
      }
      Messages.showQuestionDialog(this.curProject,
          messages.getString("ai.settings.credential.deletion.title"),
          detailedMessage.toString(),
          Messages.options(
              messages.getString("ai.messages.yes"),
              messages.getString("ai.messages.no")), 1,
          option -> {
            if (option == 0) {
              removeCredential(credentialList.getSelectedValue().getCredentialName());
            }
          });
    });

    // Configures credentialList with a list selection listener for updating display info based on selected credential
    credentialList.addListSelectionListener((e) -> {
      if (!e.getValueIsAdjusting() && credentialList.getSelectedValue() != null) {
        Credential selectedCredential = credentialList.getSelectedValue();
        displayInfo.removeAll();
        panelTemplate(selectedCredential);
        String[] used = credentialNameToProfileNameMap.get(selectedCredential).toArray(new String[]{});
        if (used.length > 0) {
          profilesLabelTitle.setText(messages.getString("credential.mgnt.used"));
          usedByList.setListData(used);
          usedByScrollPane.setVisible(true);
        } else {
          usedByList.setListData(new String[]{});
          profilesLabelTitle.setText(messages.getString("credential.mgnt.notused"));
          usedByScrollPane.setVisible(false);
        }
        editButton.setEnabled(true);
        deleteButton.setEnabled(true);
      } else {
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        displayInfo.removeAll();
      }
    });
    credentialList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Credential credential = (Credential) value;
        value = credential.getCredentialName();
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        if (!credential.isEnabled()) {
          setFont(getFont().deriveFont(Font.ITALIC));
          //setForeground(Color.DARK_GRAY);
          setToolTipText(messages.getString("ai.settings.credentials.info.is_disable_tooltip"));
        }
        if (credentialNameToProfileNameMap.get(credential) != null &&
            credentialNameToProfileNameMap.get(credential).size() > 0) {
          setFont(getFont().deriveFont(Font.BOLD));
        }

        return c;
      }
    });

  }

  /**
   * Removes a specified credential by name and updates the local cache of credentials.
   *
   * @param credential The name of the credential to be removed.
   */
  private void removeCredential(String credential) {
    credentialSvc.deleteCredential(credential)
        .thenAccept((c) -> this.updateCredentialList())
        .exceptionally(
            e -> {
              ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(this.curProject, e.getCause().getMessage()));
              return null;
            });

  }

  /**
   * Asynchronously fetches the list of credential providers from the AI credential service and updates
   * the UI components accordingly. This method retrieves the credentials, updating the credential list
   * and the display information panel based on the available credentials for the connected project.
   */
  private void updateCredentialList() {
    this.activityNotifier.start();
    credentialNameToProfileNameMap.clear();
    credentialSvc.getCredentials().thenAcceptBoth(profileSvc.getProfiles(), (credentials, profiles) -> {
      for (Credential cred : credentials) {
        List<String> pNames = profiles.stream().filter(profile -> cred.getCredentialName().equals(profile.getCredentialName()))
            .map(profile -> profile.getProfileName()).collect(Collectors.toList());
        credentialNameToProfileNameMap.put(cred, pNames);
      }
      ApplicationManager.getApplication().invokeLater(() -> { initializeUI();this.activityNotifier.stop();});
    }).exceptionally(e -> {
      {
        ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(this.curProject, e.getCause().getMessage()));
        return null;
      }
    });
  }


  /**
   * Updates the display information panel based on a selected credential.
   * This method dynamically creates and displays UI components such as labels and text fields
   * to show detailed information for the selected credential, including its name and associated username.
   *
   * @param credential The credential to display information for.
   */
  public void panelTemplate(Credential credential) {
    displayInfo.setLayout(new com.jgoodies.forms.layout.FormLayout(
        "fill:d:noGrow,left:4dlu:noGrow,fill:d:grow",
        "center:d:noGrow,top:3dlu:noGrow,center:d:noGrow,top:3dlu:noGrow,center:d:noGrow"));


    com.jgoodies.forms.layout.CellConstraints cc = new com.jgoodies.forms.layout.CellConstraints();
    displayInfo.add(new JLabel(messages.getString("ai.settings.credentials.info.credential_name")), cc.xy(1, 1));
    JTextField jt1 = new JTextField(credential.getCredentialName());
    jt1.setEditable(false);
    displayInfo.add(jt1,
        cc.xy(3, 1, com.jgoodies.forms.layout.CellConstraints.FILL, com.jgoodies.forms.layout.CellConstraints.DEFAULT));

    displayInfo.add(new JLabel(messages.getString("ai.settings.credentials.info.username")), cc.xy(1, 3));
    JTextField jt2 = new JTextField(credential.getUsername(), 10);
    jt2.setEditable(false);
    displayInfo.add(jt2,
        cc.xy(3, 3, com.jgoodies.forms.layout.CellConstraints.FILL, com.jgoodies.forms.layout.CellConstraints.DEFAULT));

    displayInfo.add(new JLabel(messages.getString("ai.settings.credentials.info.comment")), cc.xy(1, 5));
    JTextField jt3 = new JTextField(credential.getComments(), 10);
    jt3.setEditable(false);
    displayInfo.add(jt3,
        cc.xy(3, 5, com.jgoodies.forms.layout.CellConstraints.FILL, com.jgoodies.forms.layout.CellConstraints.DEFAULT));

    displayInfo.revalidate();
    displayInfo.repaint();
  }


  private void createUIComponents() {
    credentialList = new JList<Credential>() {
      public String getToolTipText(MouseEvent evt) {
        Credential cred = getModel().getElementAt(locationToIndex(evt.getPoint()));
        if (cred.isEnabled()) {
          return "";
        } else {
          return messages.getString("ai.settings.credential.not_enabled");
        }
      }

      ;
    };

  }

}
