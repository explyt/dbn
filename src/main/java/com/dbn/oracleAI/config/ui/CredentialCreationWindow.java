package com.dbn.oracleAI.config.ui;

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionRef;
import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.config.CredentialProvider;
import com.dbn.oracleAI.config.OciCredentialProvider;
import com.dbn.oracleAI.config.PasswordCredentialProvider;
import com.dbn.oracleAI.types.CredentialType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.WindowManager;


import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.CardLayout;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * A dialog window for creating new AI credentials.
 * This window allows users to input credential information, supporting different types of credentials.
 * It interacts with {@link AICredentialService} to create credentials in the system.
 */
public class CredentialCreationWindow extends JDialog {
  static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  private final AICredentialService credentialSvc;
  private JPanel contentPane;
  private JTextField credentialNameField;
  private JPanel passwordCard;
  private JComboBox<CredentialType> typeComboBox;
  private JTextField usernameField;
  private JTextField passwordField;
  private JPanel card;
  private JPanel ociCard;
  private JTextField userOcidField;
  private JTextField userTenancyOcidField;
  private JTextField privateKeyField;
  private JTextField fingerprintField;
  private JButton cancelButton;
  private JButton saveButton;
  private JLabel errorLabel;
  private ConnectionRef connection;

  /**
   * Constructs a CredentialCreationWindow dialog.
   *
   * @param connection    The connection handler associated with the current project.
   * @param credentialSvc The service used to create credentials.
   */
  public CredentialCreationWindow(ConnectionRef connection, AICredentialService credentialSvc) {
    super(WindowManager.getInstance().getFrame(connection.get().getProject()), "Oracle AI Chat Box", true);
    this.credentialSvc = credentialSvc;
    this.connection = connection;
    setContentPane(contentPane);
    setTitle(messages.getString("ai.settings.credentials.creation.title"));
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    initializeUI();
    pack();
    setLocationRelativeTo(WindowManager.getInstance().getFrame(connection.get().getProject()));
  }

  /**
   * Initializes the user interface components and event listeners for the dialog.
   */
  private void initializeUI() {
    typeComboBox.addItem(CredentialType.PASSWORD);
    typeComboBox.addItem(CredentialType.OCI);
    typeComboBox.addActionListener((e) -> {
      CardLayout cl = (CardLayout) (card.getLayout());
      cl.show(card, typeComboBox.getSelectedItem().toString());
    });
    saveButton.addActionListener((e) -> {
      addCredential();
    });
    cancelButton.addActionListener((e) -> {
      this.dispose();
    });
  }


  /**
   * Adds a credential based on the selected credential type and input fields.
   * Communicates with {@link AICredentialService} to persist the credential.
   */
  private void addCredential() {
    CredentialType credentialType = (CredentialType) typeComboBox.getSelectedItem();
    CredentialProvider credentialProvider = null;
    try {
      switch (Objects.requireNonNull(credentialType)) {
        case PASSWORD:
          credentialProvider = new PasswordCredentialProvider(credentialNameField.getText(), usernameField.getText(), passwordField.getText());
          break;
        case OCI:
          credentialProvider = new OciCredentialProvider(credentialNameField.getText(), userOcidField.getText(), userTenancyOcidField.getText(), privateKeyField.getText(), fingerprintField.getText());
      }

    credentialSvc.createCredential(credentialProvider).thenAccept((e) -> this.dispose()).exceptionally(e ->
    {
      ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(connection.get().getProject(), e.getCause().getMessage()));
      return null;
    });
    } catch (IllegalArgumentException e){
      errorLabel.setText("* " + e.getMessage());
      errorLabel.setVisible(true);
      pack();
    }
  }

  /**
   * Displays the dialog window.
   */
  public void display() {
    setVisible(true);
  }
}
