package com.dbn.oracleAI.config.ui;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.ProfileEditionWizard;
import com.dbn.oracleAI.ViewEventListener;
import com.dbn.oracleAI.WizardStepChangeEvent;
import com.dbn.oracleAI.WizardStepEventListener;
import com.dbn.oracleAI.WizardStepView;
import com.dbn.oracleAI.WizardStepViewPortProvider;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionObjectListStep;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import org.eclipse.sisu.Nullable;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.CardLayout;
import java.awt.Point;
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
  private Profile currProfile;
  private Project currProject;

  /**
   * Creates a new AI profile edition dialog
   * for profile creation.
   *
   * @param currProject current project we belong to
   */
  public ProfileEditionDialog(Project currProject) {
    this(currProject, null);
  }

  /**
   * Creates a new AI profile edition dialog
   * for profile edition
   *
   * @param currProject current project we belong to
   * @param profile     the profile to be edited through the wizard
   */
  public ProfileEditionDialog(Project currProject, @Nullable Profile profile) {
    super(WindowManager.getInstance().getFrame(currProject), "CHANGE TITLE", false);
    profileSvc = currProject.getService(DatabaseOracleAIManager.class).getProfileService();
    this.currProfile = profile;
    this.currProject = currProject;
    setModal(true);
    setContentPane(mainPane);
    setTitle(messages.getString("profiles.settings.window.title"));
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    cancelButton.addActionListener(event -> {
      dispose();
    });
    initWizard();
    initProgress();

    FORWARD = new ForwardAction(this.wizardModelView);
    COMMIT = new CommitAction(this);

    nextButton.setAction(FORWARD);

    previousButton.addActionListener(e -> {
      wizardModelView.backward();
    });
    // we never start moving backward
    previousButton.setEnabled(false);
    pack();
    setLocationRelativeTo(WindowManager.getInstance().getFrame(currProject));
    Point location = getLocation();
    location.translate(50, -50);
    this.setLocation(location);
    ;
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
  private void commitWizardView(Profile currProfile) {
    // TODO : check that data have changed to avoid
    //        calling service
    //   see this.wizardModelView.current().getProvider().isInputsChanged()
    Profile editedProfile;
    if (currProfile != null) {
      editedProfile = currProfile;
      this.wizardModel.hydrate(editedProfile);

      profileSvc.updateProfile(editedProfile).thenRun(this::dispose).exceptionally(e -> {
        ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currProject, e.getCause().getMessage()));
        return null;
      });
    } else {
      editedProfile = Profile.builder()
          .profileName("")
          .credentialName("")
          .model("").provider(ProviderType.COHERE)
          .build();
      this.wizardModel.hydrate(editedProfile);

      profileSvc.createProfile(editedProfile).thenRun(this::dispose).exceptionally(
          e -> {
            ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currProject, e.getCause().getMessage()));
            return null;
          }
      );
    }


  }

  /**
   * init the profile edition wizard
   */
  private void initWizard() {
    this.wizardModel = new ProfileEditionWizard(currProject, currProfile);
    this.wizardModelView = this.wizardModel.getView();
    this.wizardModelView.addListener(this);
    CardLayout layout = (CardLayout) this.wizardMainPane.getLayout();

    this.wizardModel.populateTo(this, this.wizardMainPane);
    layout.show(this.wizardMainPane, this.wizardModelView.current().getTitle());
  }


  public void display() {
    setVisible(true);
  }

  @Override
  public void onViewChange() {

    SwingWorker<Void, Void> worker = new SwingWorker<>() {
      @Override
      protected Void doInBackground() throws Exception {
        int start = wizardProgress.getValue();
        int end = wizardModelView.progress();
        if (start < end) {
          for (int i = start; i <= end; i++) {
            wizardProgress.setValue(i);
            Thread.sleep(5);
          }
        } else {
          for (int i = start; i >= end; i--) {
            wizardProgress.setValue(i);
            Thread.sleep(5);
          }
        }
        return null;
      }
    };
    worker.execute();
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
      if (this.currProfile == null && changedSteps.isEmpty()) {
        // we are in creation and no step have changed
        // there is not way this wizard is valid
        nextButton.setEnabled(false);
      } else {
        nextButton.setEnabled(true);
      }

    } else {
      nextButton.setAction(FORWARD);
    }
    CardLayout layout = (CardLayout) this.wizardMainPane.getLayout();
    layout.show(this.wizardMainPane, this.wizardModelView.current().getTitle());
    System.out.println("onViewChange: current view " + this.wizardModelView);
  }

  Set<WizardStepViewPortProvider> invalidSteps = new HashSet<>();
  Set<WizardStepViewPortProvider> changedSteps = new HashSet<>();

  @Override
  public void onStepChange(WizardStepChangeEvent event) {
    changedSteps.add(event.getProvider());
    // keep track of invalid steps
    if (!event.getProvider().isInputsValid()) {
      invalidSteps.add(event.getProvider());
    } else {
      invalidSteps.remove(event.getProvider());
    }
    if (event.getProvider() instanceof ProfileEditionObjectListStep) {
      if (invalidSteps.isEmpty()) {
        nextButton.setEnabled(true);
      } else {
        // TODO : use Messages
        nextButton.setToolTipText("fix errors first");
        nextButton.setEnabled(false);
      }
    }
  }

  private class ForwardAction extends AbstractAction {
    private WizardStepView view;

    public ForwardAction(WizardStepView view) {
      super("next", new ImageIcon("img/NextRecord.png"));
      this.view = view;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      this.view.forward();
    }
  }

  private class CommitAction extends AbstractAction {
    private ProfileEditionDialog dialog;

    public CommitAction(ProfileEditionDialog dialog) {
      super(currProfile != null ? "Update" : "Create");
      this.dialog = dialog;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      this.dialog.commitWizardView(currProfile);
    }
  }
}
