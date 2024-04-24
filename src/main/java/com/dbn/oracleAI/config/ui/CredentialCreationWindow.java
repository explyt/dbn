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
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
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
  private JButton keyProviderPickerButton;
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
      keyProviderPickerButton.addActionListener((e) -> {
        ProvidersSelectionCallback providersSelectionCallback = aiProviderType -> populateFields(aiProviderType.getUsername(), aiProviderType.getKey());
        AiProviderSelection aiProviderSelection = new AiProviderSelection(connection.get().getProject(), providersSelectionCallback);
        aiProviderSelection.showAndGet();
      });
      keyProviderPickerButton.setToolTipText(messages.getString("ai.settings.providers.selection.button"));
    }
  }

  private void populateFields(String username, String key) {
    typeComboBox.setSelectedItem(CredentialType.PASSWORD);
    usernameField.setText(username);
    passwordField.setText(key);
  }

  /**
   * Populate fields with the attributes of the credential to be updated
   */
  private void hydrateFields() {
    credentialNameField.setText(credential.getCredentialName());
    credentialNameField.setEnabled(false);
    //TODO find a way to distinguish between credential types
    // For now, just assuming it's password type
//    if (credential instanceof PasswordCredential) {
    typeComboBox.addItem(CredentialType.PASSWORD);
    typeComboBox.setSelectedIndex(0);
    usernameField.setText(credential.getUsername());
//    } else if (credential instanceof OciCredential) {
//      typeComboBox.addItem(CredentialType.OCI);
//      typeComboBox.setSelectedIndex(0);
//      OciCredential ociCredentialProvider = (OciCredential) credential;
//      userOcidField.setText(ociCredentialProvider.getUsername());
//      userTenancyOcidField.setText(ociCredentialProvider.getUserTenancyOCID());
//      privateKeyField.setText(ociCredentialProvider.getPrivateKey());
//      fingerprintField.setText(ociCredentialProvider.getFingerprint());
//    }
    typeComboBox.setEnabled(false);
  }


  /**
   * Collects the fields' info and sends them to the service layer to create new credential
   */
  private void doCreateAction() {
    CredentialType credentialType = (CredentialType) typeComboBox.getSelectedItem();
    Credential credential = getCredentialType(credentialType);
    ;
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
   * Based on the credential Type we picked in combobox, it create a new credential instance to be sent either for creation or update in DB
   *
   * @param credentialType
   * @return
   */
  private Credential getCredentialType(CredentialType credentialType) {
    Credential credential = null;
    switch (credentialType) {
      case PASSWORD:
        credential = new PasswordCredential(credentialNameField.getText(), usernameField.getText(), passwordField.getText());
        break;
      case OCI:
        credential = new OciCredential(credentialNameField.getText(), userOcidField.getText(), userTenancyOcidField.getText(), privateKeyField.getText(), fingerprintField.getText());
    }
    return credential;
  }

  /**
   * Collects the fields' info and sends them to the service layer to update new credential
   */
  private void doUpdateAction() {
    CredentialType credentialType = CredentialType.PASSWORD;
    Credential editedCredential = getCredentialType(credentialType);
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
   * Defines the behaviour when we click the create/update button
   * It starts by validating, and then it executes the specifies action
   */
  @Override
  protected void doOKAction() {
    super.doOKAction();
    if (credential != null) {
      doUpdateAction();
    } else {
      doCreateAction();
    }
  }

  /**
   * Defines the validation logic for the fields
   */
  @Override
  protected ValidationInfo doValidate() {
    if (credentialNameField.getText().isEmpty()) {
      return new ValidationInfo(messages.getString("ai.settings.credential.creation.validation.credentialName"), credentialNameField);
    }
    if (typeComboBox.getSelectedItem() == CredentialType.PASSWORD) {

      if (usernameField.getText().isEmpty()) {
        return new ValidationInfo(messages.getString("ai.settings.credential.creation.validation.username"), usernameField);
      }
      if (passwordField.getText().isEmpty()) {
        return new ValidationInfo(messages.getString("ai.settings.credential.creation.validation.password"), passwordField);
      }
    } else {
      if (userOcidField.getText().isEmpty()) {
        return new ValidationInfo(messages.getString("ai.settings.credential.creation.validation.userOcid"), userOcidField);
      }
      if (userTenancyOcidField.getText().isEmpty()) {
        return new ValidationInfo(messages.getString("ai.settings.credential.creation.validation.userTenancyOcid"), userTenancyOcidField);
      }
      if (privateKeyField.getText().isEmpty()) {
        return new ValidationInfo(messages.getString("ai.settings.credential.creation.validation.privateKey"), privateKeyField);
      }
      if (fingerprintField.getText().isEmpty()) {
        return new ValidationInfo(messages.getString("ai.settings.credential.creation.validation.fingerprint"), fingerprintField);
      }
    }
    return null;
  }

  /**
   * Handles the creation of the new instance and the opening of the dialog window
   *
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
