package com.dbn.oracleAI;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileUpdate;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionWizardModel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.wizard.WizardDialog;
import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AI profile edition wizard class
 */
public class ProfileEditionWizard extends WizardDialog<ProfileEditionWizardModel> {

  private static final Logger LOGGER = Logger.getInstance(ProfileEditionWizard.class.getPackageName());

  private final Profile initialProfile;
  private final Profile editedProfile;
  private static List<String> existingProfileNames;
  private final boolean isUpdate;
  private JButton finishButton;

  private final Project project;
  private final AIProfileService profileSvc;
  private final Consumer<Boolean> callback;
  private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  /**
   * Creates a new wizard
   * A profile instance is passed form one step to another.
   * In case of update the profile is pre-populated.
   * @param project the current project
   * @param profile the profile to be edited or created.
   * @param existingProfileNames list of existing profile names. used to forbid naming collision
   * @param isUpdate denote if current wizard is for an update
   * @param callback callback to be called when wizard window closes
   */
  public ProfileEditionWizard(@NotNull Project project, Profile profile, List<String> existingProfileNames, boolean isUpdate, @NotNull Consumer<Boolean> callback) {
    super(false, new ProfileEditionWizardModel(
            ResourceBundle.getBundle("Messages", Locale.getDefault()).getString("profiles.settings.window.title"), project, profile, existingProfileNames, isUpdate));
    profileSvc = project.getService(DatabaseOracleAIManager.class).getProfileService();
    this.project = project;
    this.initialProfile = new Profile(profile);
    this.editedProfile = profile;
    this.isUpdate = isUpdate;
    this.callback = callback;
    finishButton.setText(messages.getString(isUpdate ? "ai.messages.button.update" : "ai.messages.button.create"));
  }


  @Override
  protected void doOKAction() {
    LOGGER.debug("entering doOKAction");
    if (editedProfile.getProfileName().isEmpty()) {
      Messages.showErrorDialog(project, messages.getString("profile.mgmt.general_step.profile_name.validation.empty"));
    } else if (!this.isUpdate &&
            existingProfileNames.contains(editedProfile.getProfileName().trim().toUpperCase())) {
      Messages.showErrorDialog(project, messages.getString("profile.mgmt.general_step.profile_name.validation.exists"));
    } else if (editedProfile.getCredentialName().isEmpty()) {
      Messages.showErrorDialog(project, messages.getString("profile.mgmt.general_step.credential_name.validation"));
    } else if (editedProfile.getObjectList().isEmpty()) {
      Messages.showErrorDialog(project, messages.getString("profile.mgmt.object_list_step.validation"));
    } else if (initialProfile.equals(editedProfile)) {
      LOGGER.debug("profile has not changed, skipping the update");
      Messages.showErrorDialog(project, messages.getString("profile.mgmt.update.validation"));
    } else {
      commitWizardView();
      super.doOKAction();
    }
  }

  @Override
  protected JComponent createCenterPanel() {
    JComponent wizard = super.createCenterPanel();
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(wizard, BorderLayout.CENTER);
    mainPanel.setMinimumSize(new JBDimension(600, 400));
    return mainPanel;
  }

  @Override
  protected JComponent createSouthPanel() {
    JComponent wizard = super.createSouthPanel();
    for (Component component : wizard.getComponents()) {
      ((Container) component).remove(((Container) component).getComponent(4));
      JButton cancelButton = (JButton) ((Container) component).getComponent(3);
      finishButton = (JButton) ((Container) component).getComponent(2);
      ((Container) component).remove(cancelButton);
      wizard.add(cancelButton, BorderLayout.WEST);
      MatteBorder topBorder = new MatteBorder(1, 0, 0, 0, JBColor.LIGHT_GRAY);
      wizard.setBorder(topBorder);
    }
    return wizard;
  }


  private void commitWizardView() {
    LOGGER.debug("entering commitWizardView. isUpdate? " + isUpdate);
    if (isUpdate) {
      profileSvc.updateProfile(editedProfile).thenRun(() -> {
        SwingUtilities.invokeLater(() -> {
          dispose();
          callback.accept(true);
        });
      }).exceptionally(e -> {
        LOGGER.error("cannot commit profile edition wizard", e);
        SwingUtilities.invokeLater(() -> Messages.showErrorDialog(project, e.getMessage(),e.getCause().getMessage()));
        return null;
      });
    } else {
      profileSvc.createProfile(editedProfile).thenRun(() -> {
        SwingUtilities.invokeLater(() -> {
          dispose();
          callback.accept(true);
        });
      }).exceptionally(e -> {
        SwingUtilities.invokeLater(() -> Messages.showErrorDialog(project, e.getCause().getMessage()));
        return null;
      });
    }
  }

  public static void showWizard(@NotNull Project project, @Nullable Profile profile, Map<String, Profile> profileMap, @NotNull Consumer<Boolean> callback) {
    existingProfileNames = profileMap.values().stream().map(Profile::getProfileName).collect(Collectors.toList());
    SwingUtilities.invokeLater(() -> {
      Profile initialProfile = null;
      boolean isUpdate;
      if (profile != null) {
        initialProfile = profile;
        isUpdate = true;
      } else {
        initialProfile = Profile.builder().profileName("").build();
        isUpdate = false;
      }
      ProfileUpdate toBeUpdatedProfile = new ProfileUpdate(initialProfile);
      ProfileEditionWizard wizard = new ProfileEditionWizard(project, toBeUpdatedProfile, existingProfileNames, isUpdate, callback);
      wizard.show();

    });
  }
}
