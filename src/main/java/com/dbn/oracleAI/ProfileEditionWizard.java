package com.dbn.oracleAI;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionWizardModel;
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

public class ProfileEditionWizard extends WizardDialog<ProfileEditionWizardModel> {

  private final Profile initialProfile;
  private final Profile editedProfile;
  private static List<String> profileNames;
  private final boolean isUpdate;
  private JButton finishButton;

  private final Project project;
  private final AIProfileService profileSvc;
  private final Consumer<Boolean> callback;
  private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  public ProfileEditionWizard(@NotNull Project project, Profile profile, List<String> profileNames, boolean isUpdate, @NotNull Consumer<Boolean> callback) {
    super(false, new ProfileEditionWizardModel("Profile Configuration", project, profile, profileNames, isUpdate));
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
    if (editedProfile.getProfileName().isEmpty() || profileNames.contains(editedProfile.getProfileName().trim().toUpperCase()) || editedProfile.getCredentialName().isEmpty() || editedProfile.getObjectList().isEmpty()) {
      Messages.showErrorDialog(project, messages.getString("profile.mgmt.general_step.validation"));
    } else if (initialProfile.equals(editedProfile)) {
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
    if (isUpdate) {
      profileSvc.updateProfile(editedProfile).thenRun(() -> {
        SwingUtilities.invokeLater(() -> {
          dispose();
          callback.accept(true);
        });
      }).exceptionally(e -> {
        SwingUtilities.invokeLater(() -> Messages.showErrorDialog(project, e.getCause().getMessage()));
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
    profileNames = profileMap.values().stream().map(Profile::getProfileName).collect(Collectors.toList());
    SwingUtilities.invokeLater(() -> {
      Profile initialProfile;
      boolean isUpdate;
      if (profile != null) {
        initialProfile = profile;
        isUpdate = true;
      } else {
        initialProfile = Profile.builder().build();
        isUpdate = false;
      }
      ProfileEditionWizard wizard = new ProfileEditionWizard(project, initialProfile, profileNames, isUpdate, callback);
      wizard.show();

    });
  }
}
