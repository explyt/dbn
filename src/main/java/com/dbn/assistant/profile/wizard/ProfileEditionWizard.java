/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.assistant.profile.wizard;

import com.dbn.common.dispose.Failsafe;
import com.dbn.common.outcome.DialogCloseOutcomeHandler;
import com.dbn.common.outcome.OutcomeHandler;
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.diagnostics.Diagnostics;
import com.dbn.object.DBAIProfile;
import com.dbn.object.DBSchema;
import com.dbn.object.common.DBObjectUtil;
import com.dbn.object.impl.DBAIProfileImpl;
import com.dbn.object.management.ObjectManagementService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.wizard.WizardDialog;
import com.intellij.ui.wizard.WizardStep;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.Set;

import static com.dbn.common.util.Classes.simpleClassName;
import static com.dbn.nls.NlsResources.txt;

/**
 * AI profile edition wizard class
 *
 * @author Emmanuel Jannetti (Oracle)
 * @author Ayoub Aarrasse (Oracle)
 */
@Slf4j
public class ProfileEditionWizard extends WizardDialog<ProfileEditionWizardModel> implements Disposable {

  private final ProfileData initialProfile;
  private final Set<String> existingProfileNames;
  private final boolean isUpdate;
  private JButton finishButton;

  private final ConnectionRef connection;
  private String currentStepTitle;

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
  public ProfileEditionWizard(@NotNull ConnectionHandler connection, DBAIProfile profile, Set<String> existingProfileNames, boolean isUpdate, Class<ProfileEditionObjectListStep> firstStep) {
    super(false, new ProfileEditionWizardModel(
            connection, txt("cfg.assistant.title.ProfileConfiguration"), new ProfileData(profile), existingProfileNames, isUpdate,firstStep));
    this.connection = ConnectionRef.of(connection);
    this.initialProfile = new ProfileData(profile);
    this.existingProfileNames = existingProfileNames;
    this.isUpdate = isUpdate;
    finishButton.setText(txt(isUpdate ? "msg.shared.button.Update" : "msg.shared.button.Create"));
    currentStepTitle = resolveCurrentStepTitle();
  }

  private String resolveCurrentStepTitle() {
    WizardStep currentStep = myModel.getCurrentStep();
    return currentStep == null ? null : currentStep.getTitle();
  }

  @Override
  public void onStepChanged() {
    super.onStepChanged();

    String oldTitle = currentStepTitle;
    String newTitle = resolveCurrentStepTitle();
    currentStepTitle = newTitle;

    AccessibleContext accessibleContext = getWindow().getAccessibleContext();
    accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY, oldTitle, newTitle);
  }

  @Override
  protected void init() {
    super.init();
    setSize(600, 600);
  }

  private ProfileData getEditedProfile() {
    return myModel.getProfile();
  }

  @Override
  protected void doOKAction() {
    Project project = getProject();

    ProfileData editedProfile = getEditedProfile();
    if (editedProfile.getName().isEmpty()) {
      Messages.showErrorDialog(project, txt("cfg.assistant.error.ProfileNameEmpty"));
    } else if (!this.isUpdate &&
            existingProfileNames.contains(editedProfile.getName().trim().toUpperCase())) {
      Messages.showErrorDialog(project, txt("cfg.assistant.error.ProfileNameExists"));
    } else if (editedProfile.getCredentialName().isEmpty()) {
      Messages.showErrorDialog(project, txt("cfg.assistant.error.CredentialNameEmpty"));
    } else if (editedProfile.getObjectList().isEmpty()) {
      Messages.showErrorDialog(project, txt("cfg.assistant.error.ObjectInfoEmpty"));
    } else if (initialProfile.equals(editedProfile)) {
      Messages.showErrorDialog(project, txt("cfg.assistant.error.NoFieldChanges"));
    } else {
      commitWizardView();
    }
  }

  @Override
  protected @NonNls @Nullable String getDimensionServiceKey() {
    // remember last dialog size and position
    return Diagnostics.isDialogSizingReset() ? null : "DBNavigator." + simpleClassName(this);
  }

  @Override
  protected JComponent createCenterPanel() {
    JComponent wizard = super.createCenterPanel();
    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.add(wizard, BorderLayout.CENTER);
    //ainPanel.setMinimumSize(new JBDimension(600, 600));
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


  @SneakyThrows
  private void commitWizardView() {
    Project project = getProject();
    ProfileData editedProfile = getEditedProfile();

    DBSchema userSchema = Failsafe.nd(getConnection().getObjectBundle().getUserSchema());
    DBAIProfile profile = new DBAIProfileImpl(userSchema,
            editedProfile.getName(),
            editedProfile.getDescription(),
            userSchema.getCredential(editedProfile.getCredentialName()),
            editedProfile.getProvider(),
            editedProfile.getModel(),
            DBObjectUtil.objectListToJson(editedProfile.getObjectList()),
            editedProfile.getTemperature(),
            editedProfile.isEnabled());

    ObjectManagementService managementService = ObjectManagementService.getInstance(project);
    if (isUpdate)
      managementService.updateObject(profile, wizardClose()); else
      managementService.createObject(profile, wizardClose());
  }

  private OutcomeHandler wizardClose() {
    return DialogCloseOutcomeHandler.create(this);
  }


  /**
   * Show the profile creation/edition wizard
   * @param connection the connection against which the profile is edited
   * @param profile the current profile to be edited (null if creating a new one)
   * @param usedProfileNames a set of existing profile names (cannot be reused when creating new profiles)
   * @param firstStepClass if not null, the step to move to directly
   */
  public static void showWizard(@NotNull ConnectionHandler connection, @Nullable DBAIProfile profile, Set<String> usedProfileNames, Class<ProfileEditionObjectListStep> firstStepClass) {
    ProfileEditionWizard wizard = new ProfileEditionWizard(connection, profile, usedProfileNames, profile != null, firstStepClass);
    wizard.show();
  }

  private ConnectionHandler getConnection() {
    return ConnectionRef.ensure(connection);
  }

  private Project getProject() {
    return getConnection().getProject();
  }

  @Override
  public void dispose() {
    super.dispose();
    Disposer.dispose(myModel);
  }
}
