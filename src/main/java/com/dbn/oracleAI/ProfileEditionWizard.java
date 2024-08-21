package com.dbn.oracleAI;

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileUpdate;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionObjectListStep;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionWizardModel;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.wizard.WizardDialog;
import com.intellij.util.ui.JBDimension;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.dbn.nls.NlsResources.txt;

/**
 * AI profile edition wizard class
 */
@Slf4j
public class ProfileEditionWizard extends WizardDialog<ProfileEditionWizardModel> {

  private final Profile initialProfile;
  private final Profile editedProfile;
  private static List<String> existingProfileNames;
  private final boolean isUpdate;
  private JButton finishButton;

  private final Project project;
  private final ManagedObjectServiceProxy<Profile> profileSvc;
  private final Consumer<Boolean> callback;

  /**
   * Creates a new wizard
   * A profile instance is passed form one step to another.
   * In case of update the profile is pre-populated.
   *
   * @param connection                        the connection against which the profile is edited
   * @param profile                           the profile to be edited or created.
   * @param existingProfileNames              list of existing profile names. used to forbid naming collision
   * @param isUpdate                          denote if current wizard is for an update
   * @param callback                          callback to be called when wizard window closes
   * @param firstStep
   */
  public ProfileEditionWizard(@NotNull ConnectionHandler connection, Profile profile, List<String> existingProfileNames, boolean isUpdate, @NotNull Consumer<Boolean> callback, Class<ProfileEditionObjectListStep> firstStep) {
    super(false, new ProfileEditionWizardModel(
            connection, txt("profiles.settings.window.title"), profile, existingProfileNames, isUpdate,firstStep));
    profileSvc = ManagedObjectServiceProxy.getInstance(connection);
    this.project = connection.getProject();
    this.initialProfile = new Profile(profile);
    this.editedProfile = profile;
    this.isUpdate = isUpdate;
    this.callback = callback;
    finishButton.setText(txt(isUpdate ? "ai.messages.button.update" : "ai.messages.button.create"));

  }


  @Override
  protected void doOKAction() {
    log.debug("entering doOKAction");
    if (editedProfile.getProfileName().isEmpty()) {
      Messages.showErrorDialog(project, txt("profile.mgmt.general_step.profile_name.validation.empty"));
    } else if (!this.isUpdate &&
            existingProfileNames.contains(editedProfile.getProfileName().trim().toUpperCase())) {
      Messages.showErrorDialog(project, txt("profile.mgmt.general_step.profile_name.validation.exists"));
    } else if (editedProfile.getCredentialName().isEmpty()) {
      Messages.showErrorDialog(project, txt("profile.mgmt.general_step.credential_name.validation"));
    } else if (editedProfile.getObjectList().isEmpty()) {
      Messages.showErrorDialog(project, txt("profile.mgmt.object_list_step.validation"));
    } else if (initialProfile.equals(editedProfile)) {
      log.debug("profile has not changed, skipping the update");
      Messages.showErrorDialog(project, txt("profile.mgmt.update.validation"));
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
    log.debug("entering commitWizardView. isUpdate? " + isUpdate);
    if (isUpdate) {
      profileSvc.update(editedProfile).thenRun(() -> {
        SwingUtilities.invokeLater(() -> {
          dispose();
          callback.accept(true);
        });
      }).exceptionally(e -> {
        log.warn("cannot commit profile edition wizard", e);
        SwingUtilities.invokeLater(() -> Messages.showErrorDialog(project, e.getMessage(),e.getCause().getMessage()));
        return null;
      });
    } else {
      profileSvc.create(editedProfile).thenRun(() -> {
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

  /**
   * Show the profile creation/edition wizard
   * @param connection the connection against which the profile is edited
   * @param profile the current profile to be edited (null if creating a new one)
   * @param profileMap the existing profiles
   * @param callback to be called once done
   * @param firstStepClass if not null, the step to move to directly
   */
  public static void showWizard(@NotNull ConnectionHandler connection, @Nullable Profile profile, Map<String, Profile> profileMap, @NotNull Consumer<Boolean> callback, Class<ProfileEditionObjectListStep> firstStepClass) {
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
      ProfileEditionWizard wizard = new ProfileEditionWizard(connection, toBeUpdatedProfile, existingProfileNames, isUpdate, callback, firstStepClass);
      wizard.show();

    });
  }
}
