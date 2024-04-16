package com.dbn.oracleAI.config.ui;

import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.ProfileEditionWizard;
import com.dbn.oracleAI.ViewEventListener;
import com.dbn.oracleAI.WizardStepChangeEvent;
import com.dbn.oracleAI.WizardStepEventListener;
import com.dbn.oracleAI.WizardStepView;
import com.dbn.oracleAI.WizardStepViewPortProvider;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class ProfileEditionDialog extends JDialog implements ViewEventListener,
  WizardStepEventListener {

  static private final ResourceBundle messages =
    ResourceBundle.getBundle("Messages", Locale.getDefault());

  private JPanel mainPane;
  private JPanel buttonPanel;
  private JPanel wizardMainPane;
  private JButton previousButton;
  private JButton nextButton;
  private JButton cancelButton;
  private JProgressBar wizardProgress;

  private ProfileEditionWizard wizardModel;
  private WizardStepView<ProfileEditionWizard.ProfileEditionWizardStep> wizardModelView;

  private ForwardAction FORWARD;
  private CommitAction COMMIT;

  private AIProfileService profileSvc;

  /**
   * Creates a new AI profile edition dialog
   * for profile creation.
   * @param currProject current project we belong to
   */
  public ProfileEditionDialog(Project currProject) {
    this(currProject,null);
  }

  /**
   * Creates a new AI profile edition dialog
   * for profile edition
   * @param currProject current project we belong to
   * @param profile the profile to be edited through the wizard
   */
  public ProfileEditionDialog(Project currProject, Profile profile) {
    super(WindowManager.getInstance().getFrame(currProject), "CHANGE TITLE", false);
    profileSvc = currProject.getService(DatabaseOracleAIManager.class).getProfileService();
    setModal(true);
    setContentPane(mainPane);
    setTitle(messages.getString("profiles.settings.window.title"));
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    cancelButton.addActionListener(event -> {
      dispose();
    });

    initWizard(profile);
    initProgress();

    FORWARD = new ForwardAction(this.wizardModelView);
    COMMIT = new CommitAction(this);

    nextButton.setAction(FORWARD);

    previousButton.addActionListener(e -> {wizardModelView.backward();});
    // we never start moving backward
    previousButton.setEnabled(false);
    pack();
  }
  private void initProgress() {
    wizardProgress.setMinimum(0);
    wizardProgress.setMaximum(100);
    wizardProgress.setValue(this.wizardModelView.progress());
    wizardProgress.setString(this.wizardModelView.current().getTitle());
    System.out.println(this.wizardModelView.toString());
  }

  /**
   * Commits user inputs on current selected profile
   */
  private void commitWizardView() {
    // TODO : check that data have changed to avoid
    //        calling service
    //   see this.wizardModelView.current().getProvider().isInputsChanged()
    // TODO : do not use dummy values
    Profile editedProfile = Profile.builder()
                                   .profileName("")
                                   .credentialName("")
                                    .model("").provider(ProviderType.COHERE)
                                   .build();
    this.wizardModel.hydrate(editedProfile);

    //TODO : we have to check if this is a "ADD" or "EDIT"
    //       assume ADD for now
      profileSvc.addProfile(editedProfile).thenRun(() -> {

      }).exceptionally(throwable -> {
        //TODO : show error dialog
        return null;
      });

    dispose();
  }

  /**
   * init the profile edition wizard
   */
  private void initWizard(Profile profile) {
    this.wizardModel = new ProfileEditionWizard(profile);
    this.wizardModelView = this.wizardModel.getView();
    this.wizardModelView.addListener(this);
    CardLayout layout = (CardLayout)this.wizardMainPane.getLayout();

    this.wizardModel.populateTo(this, this.wizardMainPane);
    layout.show(this.wizardMainPane,this.wizardModelView.current().getTitle());
  }


  public void display() {
    setVisible(true);
  }

  @Override public void onViewChange() {
    wizardProgress.setValue(this.wizardModelView.progress());
    wizardProgress.setString(this.wizardModelView.current().getTitle());

    if (!this.wizardModelView.canBackward()) {
      previousButton.setEnabled(false);
    } else {
      previousButton.setEnabled(true);
    }

    if (!this.wizardModelView.canForward()) {
      nextButton.setAction(COMMIT);
      // we are at the end , forbud user to commit if we have
      // dirty values
      if (invalidSteps.isEmpty()) {
        nextButton.setEnabled(true);
      } else {
        // TODO : use Messages
        nextButton.setToolTipText("fix errors first");
        nextButton.setEnabled(false);
      }
    } else {
      nextButton.setAction(FORWARD);
    }
    CardLayout layout = (CardLayout)this.wizardMainPane.getLayout();
    layout.show(this.wizardMainPane,this.wizardModelView.current().getTitle());
    System.out.println("onViewChange: current view " + this.wizardModelView);
  }

  Set<WizardStepViewPortProvider> invalidSteps = new HashSet<>();
  @Override public void onStepChange(WizardStepChangeEvent event) {
    // keep track of invalid steps
    if (!event.getProvider().isInputsValid()) {
      invalidSteps.add(event.getProvider());
    } else {
      invalidSteps.remove(event.getProvider());
    }
  }

  private class ForwardAction extends AbstractAction {
    private WizardStepView view;
    public ForwardAction(WizardStepView view) {
      super("next", new ImageIcon("img/NextRecord.png"));
      this.view = view;
    }

    @Override public void actionPerformed(ActionEvent e) {
      this.view.forward();
    }
  }
  private class CommitAction extends AbstractAction {
    private ProfileEditionDialog dialog;
    public CommitAction(ProfileEditionDialog dialog) {
      super("Finish");
      this.dialog = dialog;
    }

    @Override public void actionPerformed(ActionEvent e) {
      this.dialog.commitWizardView();
    }
  }
}
