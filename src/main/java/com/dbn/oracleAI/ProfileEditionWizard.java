/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI;

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ProfileUpdate;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionObjectListStep;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionWizardModel;
import com.dbn.oracleAI.service.AIProfileService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.wizard.WizardDialog;
import com.intellij.util.ui.JBDimension;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.Set;

import static com.dbn.nls.NlsResources.txt;

/**
 * AI profile edition wizard class
 *
 * @author Emmanuel Jannetti (emmanuel.jannetti@oracle.com)
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
@Slf4j
public class ProfileEditionWizard extends WizardDialog<ProfileEditionWizardModel> implements Disposable {

  private final Profile initialProfile;
  private final Profile editedProfile;
  private final Set<String> existingProfileNames;
  private final boolean isUpdate;
  private JButton finishButton;

  private final Project project;
  private final AIProfileService profileSvc;

  /**
   * Creates a new wizard
   * A profile instance is passed form one step to another.
   * In case of update the profile is pre-populated.
   *
   * @param connection                        the connection against which the profile is edited
   * @param profile                           the profile to be edited or created.
   * @param existingProfileNames              list of existing profile names. used to forbid naming collision
   * @param isUpdate                          denote if current wizard is for an update
   * @param firstStep
   */
  public ProfileEditionWizard(@NotNull ConnectionHandler connection, Profile profile, Set<String> existingProfileNames, boolean isUpdate, Class<ProfileEditionObjectListStep> firstStep) {
    super(false, new ProfileEditionWizardModel(
            connection, txt("profiles.settings.window.title"), profile, existingProfileNames, isUpdate,firstStep));
    this.profileSvc = AIProfileService.getInstance(connection);
    this.project = connection.getProject();
    this.initialProfile = new Profile(profile);
    this.editedProfile = profile;
    this.existingProfileNames = existingProfileNames;
    this.isUpdate = isUpdate;
    finishButton.setText(txt(isUpdate ? "ai.messages.button.update" : "ai.messages.button.create"));
    Disposer.register(this, myModel);
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
   * @param usedProfileNames a set of existing profile names (cannot be reused when creating new profiles)
   * @param firstStepClass if not null, the step to move to directly
   */
  public static void showWizard(@NotNull ConnectionHandler connection, @Nullable Profile profile, Set<String> usedProfileNames, Class<ProfileEditionObjectListStep> firstStepClass) {
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
      ProfileEditionWizard wizard = new ProfileEditionWizard(connection, toBeUpdatedProfile, usedProfileNames, isUpdate, firstStepClass);
      wizard.show();

    });
  }

  @Override
  public void dispose() {
    super.dispose();
  }
}
