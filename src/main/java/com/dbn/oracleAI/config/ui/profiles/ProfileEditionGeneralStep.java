package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.common.icon.Icons;
import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ui.CredentialCreationCallback;
import com.dbn.oracleAI.config.ui.CredentialCreationWindow;
import com.dbn.oracleAI.config.ui.ProfileNameVerifier;
import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import org.jetbrains.annotations.Nullable;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Profile edition general step for edition wizard.
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionGeneralStep extends WizardStep<ProfileEditionWizardModel> {
  private JPanel profileEditionGeneralMainPane;
  private JTextField nameTextField;
  private JComboBox<String> credentialComboBox;
  private JTextField descriptionTextField;
  private JButton createCredButton;
  private final AICredentialService credentialSvc;
  private final Project project;
  private Profile profile;
  private final boolean isUpdate;
  private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  public ProfileEditionGeneralStep(Project project, Profile profile, boolean isUpdate) {
    super(ResourceBundle.getBundle("Messages", Locale.getDefault()).getString("profile.mgmt.general_step.title"));
    this.project = project;
    this.profile = profile;
    this.isUpdate = isUpdate;
    this.credentialSvc = project.getService(DatabaseOracleAIManager.class).getCredentialService();

    initializeUI();
    addValidationListener();
    populateCredentials();
  }

  private void initializeUI() {
    createCredButton.setIcon(Icons.ACTION_ADD);
    createCredButton.setToolTipText(messages.getString("ai.settings.credential.adding.tooltip"));
    createCredButton.addActionListener((e) -> {
      CredentialCreationCallback callback = this::populateCredentials;
      CredentialCreationWindow win = new CredentialCreationWindow(project, credentialSvc, null, callback);
      win.showAndGet();
    });

    if (isUpdate) {
      nameTextField.setText(profile.getProfileName());
      descriptionTextField.setText(profile.getDescription());
      credentialComboBox.addItem(profile.getCredentialName());
      credentialComboBox.setEnabled(false);
    }
  }

  private void addValidationListener() {
    nameTextField.setInputVerifier(new ProfileNameVerifier());
    credentialComboBox.setInputVerifier(new CredentialSelectedVerifier());
    nameTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }

      public void removeUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }

      public void insertUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }
    });
    credentialComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        InputVerifier verifier = credentialComboBox.getInputVerifier();
        if (verifier != null) {
          verifier.verify(credentialComboBox);
        }
      }
    });

  }

  private void populateCredentials() {
    credentialSvc.getCredentials().thenAccept(credentialProviderList -> {
      SwingUtilities.invokeLater(() -> {
        credentialComboBox.removeAllItems();
        for (Credential credential : credentialProviderList) {
          credentialComboBox.addItem(credential.getCredentialName());
        }
        credentialComboBox.setSelectedIndex(0);
      });
    });
  }

  @Override
  public JComponent prepare(WizardNavigationState wizardNavigationState) {
    return profileEditionGeneralMainPane;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return nameTextField;
  }

  @Override
  public WizardStep<ProfileEditionWizardModel> onNext(ProfileEditionWizardModel model) {
    nameTextField.getInputVerifier().verify(nameTextField);
    credentialComboBox.getInputVerifier().verify(credentialComboBox);
    profile.setProfileName(nameTextField.getText());
    profile.setCredentialName((String) credentialComboBox.getSelectedItem());
    profile.setDescription(descriptionTextField.getText());


    return super.onNext(model);
  }

}
