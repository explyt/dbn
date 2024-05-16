package com.dbn.oracleAI.config.ui;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.AICredentialServiceImpl;
import com.dbn.oracleAI.config.AIProviders.AIProviderType;
import com.dbn.oracleAI.config.AIProviders.AIProvidersGeneralSettings;
import com.dbn.oracleAI.config.AIProviders.AIProvidersSettings;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.OciCredential;
import com.dbn.oracleAI.config.PasswordCredential;
import com.dbn.oracleAI.types.CredentialType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * A dialog window for creating new AI credentials.
 * This window allows users to input credential information, supporting different types of credentials.
 * It interacts with {@link AICredentialServiceImpl} to create credentials in the system.
 */
public class CredentialCreationWindow extends DialogWrapper {
  static private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  private final AICredentialService credentialSvc;
  private JPanel contentPane;
  private JTextField credentialNameField;
  private List<String> existingCredentialNames = new ArrayList();
  private JPanel passwordCard;
  private JComboBox<CredentialType> credentialTypeComboBox;
  private JTextField passwordCredentialUsernameField;
  private JTextField passwordCredentialPasswordField;
  private JPanel credentialAttributesPane;
  private JPanel ociCard;
  private JTextField OCICredentialUserOcidField;
  private JTextField OCICredentialUserTenancyOcidField;
  private JTextField OCICredentialPrivateKeyField;
  private JTextField OCICredentialFingerprintField;
  private JButton keyProviderPickerButton;
  private JPanel credentialGeneralPane;
  private JCheckBox saveInfoCheckBox;
  private JLabel errorLabel;
  private Project project;
  private Credential credential;
  private CredentialCreationCallback creationCallback;

  /**
   *
   *
   * @param connection
   * @param credentialSvc The service used to create credentials.
   */
  /**
   * Constructs a CredentialCreationWindow dialog.
   *
   * @param credentialSvc    The service used to create credentials.
   * @param credential       the credential to be edited, can be null in case of credential creation
   * @param creationCallback the callback to validate creation/edition
   */
  public CredentialCreationWindow(Project project, AICredentialService credentialSvc, @Nullable Credential credential, CredentialCreationCallback creationCallback) {
    super(true);
    this.credentialSvc = credentialSvc;
    this.project = project;
    this.credential = credential;
    this.creationCallback = creationCallback;
    init();
    setTitle(messages.getString("ai.settings.credentials.creation.title"));
    initializeUI();
    pack();
  }

  /**
   * Set the existing credential name list.
   * That list is used during validation
   *
   * @param names the list of names
   */
  public void setExistingCredentialNames(List<String> names) {
    if (names == null) {
      throw new IllegalArgumentException("cannot be null");
    }
    this.existingCredentialNames = names;
  }

  /**
   * Initializes the user interface components and event listeners for the dialog.
   */
  private void initializeUI() {
    saveInfoCheckBox.setText(messages.getString("ai.settings.credentials.info.save"));
    if (credential != null) {
      hydrateFields();
    } else {
      credentialTypeComboBox.addItem(CredentialType.PASSWORD);
      credentialTypeComboBox.addItem(CredentialType.OCI);
      credentialTypeComboBox.addActionListener((e) -> {
        if (credentialTypeComboBox.getSelectedItem() == CredentialType.PASSWORD) saveInfoCheckBox.setVisible(true);
        else saveInfoCheckBox.setVisible(false);
        CardLayout cl = (CardLayout) (credentialAttributesPane.getLayout());
        cl.show(credentialAttributesPane, credentialTypeComboBox.getSelectedItem().toString());
      });
    }
    keyProviderPickerButton.addActionListener((e) -> {
      ProvidersSelectionCallback providersSelectionCallback = aiProviderType -> populateFields(aiProviderType.getUsername(), aiProviderType.getKey());
      AiProviderSelection aiProviderSelection = new AiProviderSelection(project, providersSelectionCallback);
      aiProviderSelection.showAndGet();

    });
  }

  private void populateFields(String username, String key) {
    credentialTypeComboBox.setSelectedItem(CredentialType.PASSWORD);
    passwordCredentialUsernameField.setText(username);
    passwordCredentialPasswordField.setText(key);
  }

  /**
   * Populate fields with the attributes of the credential to be updated
   */
  private void hydrateFields() {
    credentialNameField.setText(credential.getCredentialName());
    credentialNameField.setEnabled(false);
    if (credential instanceof PasswordCredential) {
      credentialTypeComboBox.addItem(CredentialType.PASSWORD);
      passwordCredentialUsernameField.setText(credential.getUsername());
    } else if (credential instanceof OciCredential) {
      credentialTypeComboBox.addItem(CredentialType.OCI);
      OciCredential ociCredentialProvider = (OciCredential) credential;
      OCICredentialUserOcidField.setText(ociCredentialProvider.getUsername());
      OCICredentialUserTenancyOcidField.setText(ociCredentialProvider.getUserTenancyOCID());
      OCICredentialPrivateKeyField.setText(ociCredentialProvider.getPrivateKey());
      OCICredentialFingerprintField.setText(ociCredentialProvider.getFingerprint());
    }
    credentialTypeComboBox.addItem(CredentialType.PASSWORD);
    credentialTypeComboBox.setEnabled(false);
  }


  /**
   * Collects the fields' info and sends them to the service layer to create new credential
   */
  private void doCreateAction() {
    CredentialType credentialType = (CredentialType) credentialTypeComboBox.getSelectedItem();
    Credential credential = null;
    switch (credentialType) {
      case PASSWORD:
        credential = new PasswordCredential(credentialNameField.getText(), passwordCredentialUsernameField.getText(), passwordCredentialPasswordField.getText());
        break;
      case OCI:
        credential = new OciCredential(credentialNameField.getText(), OCICredentialUserOcidField.getText(),
            OCICredentialUserTenancyOcidField.getText(), OCICredentialPrivateKeyField.getText(), OCICredentialFingerprintField.getText());
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
        Messages.showErrorDialog(project, e.getCause().getMessage());
      });
      return null;
    });

  }

  /**
   * Collects the fields' info and sends them to the service layer to update new credential
   */
  private void doUpdateAction() {
    CredentialType credentialType = CredentialType.valueOf(credentialTypeComboBox.getSelectedItem().toString());
    Credential editedCredential = null;
    switch (credentialType) {
      case PASSWORD:
        editedCredential = new PasswordCredential(credentialNameField.getText(), passwordCredentialUsernameField.getText(), passwordCredentialPasswordField.getText());
        break;
      case OCI:
        editedCredential = new OciCredential(credentialNameField.getText(), OCICredentialUserOcidField.getText(),
            OCICredentialUserTenancyOcidField.getText(), OCICredentialPrivateKeyField.getText(), OCICredentialFingerprintField.getText());
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
        Messages.showErrorDialog(project, e.getCause().getMessage());
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
    if (saveInfoCheckBox.isSelected() && credentialTypeComboBox.getSelectedItem() == CredentialType.PASSWORD) {
      saveProviderInfo();
    }
    if (credential != null) {
      doUpdateAction();
    } else {
      doCreateAction();
    }
  }

  @Override
  protected Action @NotNull [] createActions() {
    super.setOKButtonText(messages.getString(credential != null ? "ai.messages.button.update" : "ai.messages.button.create"));

    return super.createActions();
  }

  /**
   * Defines the validation logic for the fields
   */
  @Override
  protected ValidationInfo doValidate() {
    if (credentialNameField.getText().isEmpty()) {
      return new ValidationInfo(messages.getString("ai.settings.credentials.info.credential_name.validation_error_1"),
          credentialNameField);
    }
    if (this.existingCredentialNames.contains(credentialNameField.getText().toUpperCase()) && credential == null) {
      return new ValidationInfo(messages.getString("ai.settings.credentials.info.credential_name.validation_error_2"),
          credentialNameField);
    }

    switch (CredentialType.valueOf(credentialTypeComboBox.getSelectedItem().toString())) {
      case PASSWORD:
        return doPasswordCredentialValidate();
      case OCI:
        return doOCICredentialValidate();
    }

    return null;
  }

  private void saveProviderInfo() {
    AIProvidersGeneralSettings settings = AIProvidersSettings.getInstance(project).getGeneralSettings();
    AIProviderType aiProviderType = new AIProviderType();
    aiProviderType.setCredentialName(credentialNameField.getText());
    aiProviderType.setUsername(passwordCredentialUsernameField.getText());
    aiProviderType.setKey(passwordCredentialPasswordField.getText());
    settings.getAIProviderTypes().add(aiProviderType);
  }

  private ValidationInfo doOCICredentialValidate() {
    if (OCICredentialUserOcidField.getText().isEmpty()) {
      return new ValidationInfo(messages.getString("ai.settings.credentials.oci.info.user_ocid.validation_error_1"), OCICredentialUserOcidField);
    }
    if (!OCICredentialUserOcidField.getText().startsWith("ocid1.user.oc1.")) {
      return new ValidationInfo(
          messages.getString("ai.settings.credentials.oci.info.user_ocid.validation_error_2"),
          OCICredentialUserOcidField);
    }
    if (OCICredentialUserTenancyOcidField.getText().isEmpty()) {
      return new ValidationInfo(
          messages.getString("ai.settings.credentials.oci.info.tenancy_ocid.validation_error_1"),
          OCICredentialUserTenancyOcidField);
    }
    if (!OCICredentialUserTenancyOcidField.getText().startsWith("ocid1.tenancy.oc1.")) {
      return new ValidationInfo(
          messages.getString("ai.settings.credentials.oci.info.tenancy_ocid.validation_error_2"),
          OCICredentialUserTenancyOcidField);
    }
    if (OCICredentialFingerprintField.getText().isEmpty()) {
      return new ValidationInfo(messages.getString("ai.settings.credentials.oci.info.fingerprint.validation_error_1"),
          OCICredentialFingerprintField);
    }
    if (OCICredentialPrivateKeyField.getText().isEmpty()) {
      return new ValidationInfo(messages.getString("ai.settings.credentials.oci.info.private_key.validation_error_1"), OCICredentialPrivateKeyField);
    }
    return null;
  }

  private ValidationInfo doPasswordCredentialValidate() {
    if (passwordCredentialUsernameField.getText().isEmpty()) {
      return new ValidationInfo(messages.getString("ai.settings.credentials.info.username.validation_error_1"),
          passwordCredentialUsernameField);
    }
    if (passwordCredentialPasswordField.getText().isEmpty()) {
      return new ValidationInfo(messages.getString("ai.settings.credentials.info.password.validation_error_1")
          , passwordCredentialPasswordField);
    }
    return null;
  }


  @Override
  protected @Nullable JComponent createCenterPanel() {
    return contentPane;
  }


}
