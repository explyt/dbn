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

import com.dbn.connection.ConnectionHandler;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.wizard.WizardModel;
import com.intellij.ui.wizard.WizardStep;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is where we add the step we want in our wizard
 */
@Getter
public class ProfileEditionWizardModel extends WizardModel implements Disposable {
  // need to keep our own list as WizardModel do not expose it.
  List<WizardStep> mysteps = null;
  private final ProfileData profile;
  public ProfileEditionWizardModel(ConnectionHandler connection, String title, ProfileData profile, Set<String> profileNames, boolean isUpdate, Class<ProfileEditionObjectListStep> firstStep) {
    super(title);
    this.profile = profile;
    mysteps = List.of(
            new ProfileEditionGeneralStep(connection, profile, profileNames, isUpdate),
            new ProfileEditionProviderStep(connection, profile, isUpdate),
            new ProfileEditionObjectListStep(connection, profile, isUpdate));

    mysteps.forEach(s->add(s));
    mysteps.forEach(s-> Disposer.register(this, (Disposable) s));
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

  @Override
  public void dispose() {
    // TODO dispose UI resources
  }
}
