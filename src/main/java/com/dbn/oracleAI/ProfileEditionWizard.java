package com.dbn.oracleAI;

import com.dbn.oracleAI.config.ui.profiles.ProfileEditionGeneralStep;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionObjectListStep;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionProviderStep;
import lombok.Getter;
import lombok.ToString;

import javax.swing.JPanel;
import java.util.LinkedList;

public class ProfileEditionWizard {


  private LinkedList<ProfileEditionWizardStep> steps = new LinkedList<ProfileEditionWizardStep>();

  public int getStepCount() {
    return steps.size();
  }

  public WizardStepView<ProfileEditionWizardStep> getView() {
    return new WizardStepView(this.steps);
  }

  public void populateTo(JPanel panel) {
    this.steps.forEach(step -> {
      panel.add(step.getViewPort(),step.getTitle());
    });

  }

  @Getter @ToString static public class ProfileEditionWizardStep
    implements WizardStep {
    private String title;
    private WizardStepViewPortProvider provider;
    public ProfileEditionWizardStep(String title,
                                    WizardStepViewPortProvider provider) {
      assert title != null:"title cannot be null";
      assert provider != null:"provider cannot be null";
      this.title = title;
      this.provider = provider;
    }

    @Override public JPanel getViewPort() {
      return this.provider.getPanel();
    }
  }

  public ProfileEditionWizard() {
    // fake it for now
    this.steps = new LinkedList<>();
    this.steps.add(new ProfileEditionWizardStep("general",
                                                new ProfileEditionGeneralStep()));
    this.steps.add(new ProfileEditionWizardStep("provider",
                                                new ProfileEditionProviderStep()));
    this.steps.add(new ProfileEditionWizardStep("object list",
                                                new ProfileEditionObjectListStep()));

  }
}
