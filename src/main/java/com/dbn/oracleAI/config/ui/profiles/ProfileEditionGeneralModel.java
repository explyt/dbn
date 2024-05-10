package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.oracleAI.config.Profile;
import com.intellij.openapi.project.Project;
import com.intellij.ui.wizard.WizardModel;

public class ProfileEditionGeneralModel extends WizardModel {
  public ProfileEditionGeneralModel(String title, Project project, Profile profile, boolean isUpdate) {
    super(title);
    add(new ProfileEditionGeneralStep(project, profile, isUpdate));
    add(new ProfileEditionProviderStep(project, profile, isUpdate));
    add(new ProfileEditionObjectListStep(project, profile, isUpdate));
  }


}
