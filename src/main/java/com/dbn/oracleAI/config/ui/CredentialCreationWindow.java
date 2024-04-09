package com.dbn.oracleAI.config.ui;

import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.types.CredentialType;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * A dialog window for creating new AI credentials.
 * This window allows users to input credential information, supporting different types of credentials.
 * It interacts with {@link AICredentialService} to create credentials in the system.
 */
public class CredentialCreationWindow extends JDialog {
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

  /**
   * Constructs a CredentialCreationWindow dialog.
   *
   * @param connection The connection handler associated with the current project.
   * @param credentialSvc The service used to create credentials.
   */
  public CredentialCreationWindow(ConnectionHandler connection, AICredentialService credentialSvc){
    super(WindowManager.getInstance().getFrame(connection.getProject()), "Oracle AI Chat Box", true);
    this.credentialSvc = credentialSvc;
    setContentPane(contentPane);
    setTitle("Credential Provider Creation");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    initializeUI();
    pack();
    setLocationRelativeTo(WindowManager.getInstance().getFrame(connection.getProject()));
  }

  /**
   * Initializes the user interface components and event listeners for the dialog.
   */
  private void initializeUI() {
    typeComboBox.addItem(CredentialType.PASSWORD);
    typeComboBox.addItem(CredentialType.OCI);
    typeComboBox.addActionListener((e)->{
      CardLayout cl = (CardLayout)(card.getLayout());
      cl.show(card, typeComboBox.getSelectedItem().toString());
    });
    saveButton.addActionListener((e)->{
      addCredential();
    });
    cancelButton.addActionListener((e)->{
      this.dispose();
    });
  }

  /**
   * Adds a credential based on the selected credential type and input fields.
   * Communicates with {@link AICredentialService} to persist the credential.
   */
  private void addCredential(){
    CredentialType credentialType = (CredentialType) typeComboBox.getSelectedItem();
    switch (Objects.requireNonNull(credentialType)){
      case PASSWORD:
        credentialSvc.createPasswordCredential(credentialNameField.getText(), usernameField.getText(), passwordField.getText()).thenRun(this::dispose);
        break;
      case OCI:
        credentialSvc.createOCICredential(credentialNameField.getText(), userOcidField.getText(), userTenancyOcidField.getText(), privateKeyField.getText(), fingerprintField.getText()).thenRun(this::dispose);
    }
  }

  /**
   * Displays the dialog window.
   */
  public void display(){
    setVisible(true);
  }
}
