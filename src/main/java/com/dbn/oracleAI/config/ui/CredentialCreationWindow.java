package com.dbn.oracleAI.config.ui;

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionRef;
import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.OciCredential;
import com.dbn.oracleAI.config.PasswordCredential;
import com.dbn.oracleAI.types.CredentialType;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A dialog window for creating new AI credentials.
 * This window allows users to input credential information, supporting different types of credentials.
 * It interacts with {@link AICredentialService} to create credentials in the system.
 */
public class CredentialCreationWindow extends DialogWrapper {
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
  private JLabel errorLabel;
  private ConnectionRef connection;
  private Credential credential;
  private CredentialCreationCallback creationCallback;

  /**
   * Constructs a CredentialCreationWindow dialog.
   *
   * @param connection    The connection handler associated with the current project.
   * @param credentialSvc The service used to create credentials.
   */
  public CredentialCreationWindow(ConnectionRef connection, AICredentialService credentialSvc, @Nullable Credential credential, CredentialCreationCallback creationCallback) {
    super(true);
    this.credentialSvc = credentialSvc;
    this.connection = connection;
    this.credential = credential;
    this.creationCallback = creationCallback;
    init();
    setTitle(messages.getString("ai.settings.credentials.creation.title"));
    initializeUI();
    pack();
  }

  /**
   * Initializes the user interface components and event listeners for the dialog.
   */
  private void initializeUI() {
    if (credential != null) {
      hydrateFields();
    } else {
      typeComboBox.addItem(CredentialType.PASSWORD);
      typeComboBox.addItem(CredentialType.OCI);
      typeComboBox.addActionListener((e) -> {
        CardLayout cl = (CardLayout) (card.getLayout());
        cl.show(card, typeComboBox.getSelectedItem().toString());
      });
    }
  }

  /**
   * Populate fields with the attributes of the credential to be updated
   */
  private void hydrateFields() {
    credentialNameField.setText(credential.getCredentialName());
    credentialNameField.setEnabled(false);
    if(credential instanceof PasswordCredential){
      typeComboBox.addItem(CredentialType.PASSWORD);
      usernameField.setText(credential.getUsername());
    } else if (credential instanceof OciCredential){
      typeComboBox.addItem(CredentialType.OCI);
      OciCredential ociCredentialProvider = (OciCredential) credential;
      userOcidField.setText(ociCredentialProvider.getUsername());
      userTenancyOcidField.setText(ociCredentialProvider.getUserTenancyOCID());
      privateKeyField.setText(ociCredentialProvider.getPrivateKey());
      fingerprintField.setText(ociCredentialProvider.getFingerprint());
    }
    typeComboBox.setEnabled(false);
  }


  /**
   * Define the possible actions of this dialog window
   */
  @NotNull
  @Override
  protected Action @NotNull [] createActions() {
    super.createActions();

    // Defines the action to either create or update credential
    Action commitAction;
    if(credential ==null){
      commitAction = new AbstractAction("Create") {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (doValidate() == null) {
          doCreateAction();
        }
      }
    };} else{
      commitAction = new AbstractAction("Update") {
        @Override
        public void actionPerformed(ActionEvent e) {
          if (doValidate() == null) {
            doUpdateAction();
          }
        }
      };
    }

    // Defines action to cancel the operation and close the window
    Action cancelAction = new AbstractAction("Cancel") {
      @Override
      public void actionPerformed(ActionEvent e) {
        doCancelAction();
      }
    };
    return new Action[]{commitAction, cancelAction};
  }

  /**
   * Collects the fields' info and sends them to the service layer to create new credential
   */
  private void doCreateAction() {
    CredentialType credentialType = (CredentialType) typeComboBox.getSelectedItem();
    Credential credential = null;
    switch (credentialType) {
      case PASSWORD:
        credential = new PasswordCredential(credentialNameField.getText(), usernameField.getText(), passwordField.getText());
        break;
      case OCI:
        credential = new OciCredential(credentialNameField.getText(), "ocidField", "tenancyOcid", "privateKey", "fingerprint");
    }
    credentialSvc.createCredential(credential).thenAccept((e) -> {
      SwingUtilities.invokeLater(() -> {
        if (creationCallback != null) {
          creationCallback.onCredentialCreated();
        }
        close(0);
      });
    }).exceptionally(e -> {
      SwingUtilities.invokeLater(() -> {
        Messages.showErrorDialog(connection.get().getProject(), e.getCause().getMessage());
      });
      return null;
    });

  }

  /**
   * Collects the fields' info and sends them to the service layer to update new credential
   */
  private void doUpdateAction() {
    CredentialType credentialType = CredentialType.PASSWORD;
    Credential editedCredential = null;
    switch (credentialType) {
      case PASSWORD:
        editedCredential = new PasswordCredential(credentialNameField.getText(), usernameField.getText(), passwordField.getText());
        break;
      case OCI:
        editedCredential = new OciCredential(credentialNameField.getText(), "ocidField", "tenancyOcid", "privateKey", "fingerprint");
    }
    credentialSvc.updateCredential(editedCredential).thenAccept((e) -> {
      SwingUtilities.invokeLater(() -> {
        if (creationCallback != null) {
          creationCallback.onCredentialCreated();
        }
        close(0);
      });
    }).exceptionally(e -> {
      SwingUtilities.invokeLater(() -> {
        Messages.showErrorDialog(connection.get().getProject(), e.getCause().getMessage());
      });
      return null;
    });

  }

  /**
   * Defines the validation logic for the fields
   */
  @Override
  protected ValidationInfo doValidate() {
    if (credentialNameField.getText().isEmpty()) {
      return new ValidationInfo("Credential name cannot be empty", credentialNameField);
    }
    if (passwordField.getText().isEmpty()) {
      return new ValidationInfo("Password cannot be empty", passwordField);
    }
    return null;
  }

  /**
   * Handles the creation of the new instance and the opening of the dialog window
   * @param connection
   * @param credentialSvc
   * @param credential
   * @param creationCallback to refresh the credentials list once we created a new one
   */
  public static void showDialog(ConnectionRef connection, AICredentialService credentialSvc, Credential credential, CredentialCreationCallback creationCallback) {
    CredentialCreationWindow dialog = new CredentialCreationWindow(connection, credentialSvc, credential, creationCallback);
    dialog.showAndGet();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return contentPane;
  }
}
