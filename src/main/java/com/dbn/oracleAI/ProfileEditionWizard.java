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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ProfileEditionWizard extends WizardDialog<ProfileEditionWizardModel> {

  private final Profile profile;
  private final boolean isUpdate;
  private JButton finishButton;

  private final Project project;
  private final AIProfileService profileSvc;
  private final Consumer<Boolean> callback;
  private final ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  public ProfileEditionWizard(@NotNull Project project, Profile initialProfile, boolean isUpdate, @NotNull Consumer<Boolean> callback) {
    super(false, new ProfileEditionWizardModel("Profile Configuration", project, initialProfile, isUpdate));
    profileSvc = project.getService(DatabaseOracleAIManager.class).getProfileService();
    this.project = project;
    this.profile = initialProfile;
    this.isUpdate = isUpdate;
    this.callback = callback;
    finishButton.setText(messages.getString(isUpdate ? "ai.messages.button.update" : "ai.messages.button.create"));
  }


  @Override
  protected void doOKAction() {
    if (profile.getCredentialName().isEmpty() || profile.getObjectList().isEmpty()) {
      Messages.showErrorDialog(project, messages.getString("profile.mgmt.general_step.validation"));
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
      profileSvc.updateProfile(profile).thenRun(() -> {
        SwingUtilities.invokeLater(() -> {
          dispose();
          callback.accept(true);
        });
      }).exceptionally(e -> {
        SwingUtilities.invokeLater(() -> Messages.showErrorDialog(project, e.getCause().getMessage()));
        return null;
      });
    } else {
      profileSvc.createProfile(profile).thenRun(() -> {
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

  public static void showWizard(@NotNull Project project, @Nullable Profile profile, @NotNull Consumer<Boolean> callback) {
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
      ProfileEditionWizard wizard = new ProfileEditionWizard(project, initialProfile, isUpdate, callback);
      wizard.show();

    });
  }
}
