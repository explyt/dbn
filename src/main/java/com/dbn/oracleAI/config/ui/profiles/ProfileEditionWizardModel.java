package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.config.Profile;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardStep;

import java.util.List;
import java.util.Optional;

/**
 * This is where we add the step we want in our wizard
 */
public class ProfileEditionWizardModel extends WizardModel {
  // need to keep our own list as WizardModel do not expose it.
  List<WizardStep> mysteps = null;
  public ProfileEditionWizardModel(ConnectionHandler connection, String title, Profile profile, List<String> profileNames, boolean isUpdate, Class<ProfileEditionObjectListStep> firstStep) {
    super(title);
    mysteps = List.of(
            new ProfileEditionGeneralStep(connection, profile, profileNames, isUpdate),
            new ProfileEditionProviderStep(connection, profile, isUpdate),
            new ProfileEditionObjectListStep(connection, profile, isUpdate));
    mysteps.forEach(s->add(s));
    if (firstStep != null) {
      moveToStep(firstStep);
    }
  }

  /**
   * Moves the wizard to a given step.
   *
   * @param stepClass the class implementing the step
   * @throws IllegalArgumentException if a step with a specified class name is not found.
   */
  private void moveToStep(Class stepClass) throws IllegalArgumentException {
    // first locate the right step class
    Optional<WizardStep> theOne = mysteps.stream().filter(s->s.getClass().equals(stepClass)).findFirst();
    if (theOne.isEmpty()) {
      throw new IllegalArgumentException("unknown step class");
    }
    int index = this.getStepIndex(theOne.get());
    for (int i = 0;i<index;i++) {
      this.next();
    }
  }

}
