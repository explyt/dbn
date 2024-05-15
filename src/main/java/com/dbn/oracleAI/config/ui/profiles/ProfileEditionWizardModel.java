package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.Profile;
import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardModel;

import java.util.List;

/**
 * This is where we add the step we want in our wizard
 */
public class ProfileEditionWizardModel extends WizardModel {
  public ProfileEditionWizardModel(String title, Project project, Profile profile, List<String> profileNames, boolean isUpdate) {
    super(title);
    add(new ProfileEditionGeneralStep(project, profile, profileNames, isUpdate));
    add(new ProfileEditionProviderStep(project, profile, isUpdate));
    add(new ProfileEditionObjectListStep(project, profile, isUpdate));
  }


}
