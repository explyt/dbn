package com.dbn.oracleAI.config.ui;

import com.dbn.oracleAI.ProfileEditionWizard;
import com.dbn.oracleAI.ViewEventListener;
import com.dbn.oracleAI.WizardStepView;
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
import java.util.Locale;
import java.util.ResourceBundle;

public class ProfileEditionDialog extends JDialog implements ViewEventListener {

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

  public ProfileEditionDialog(Project currProject) {
    super(WindowManager.getInstance().getFrame(currProject), "CHANGE TITLE", false);

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


  private void commitWizardView() {
    // enough for now

    dispose();
  }

  private void initWizard() {
    this.wizardModel = new ProfileEditionWizard();
    this.wizardModelView = this.wizardModel.getView();
    this.wizardModelView.addListener(this);
    CardLayout layout = (CardLayout)this.wizardMainPane.getLayout();

    this.wizardModel.populateTo(this.wizardMainPane);
    layout.show(this.wizardMainPane,this.wizardModelView.current().getTitle());
  }


  public void display() {
    setVisible(true);
  }

  @Override public void notifyViewChange() {
    wizardProgress.setValue(this.wizardModelView.progress());
    wizardProgress.setString(this.wizardModelView.current().getTitle());

    if (!this.wizardModelView.canBackward()) {
      previousButton.setEnabled(false);
    } else {
      previousButton.setEnabled(true);
    }

    if (!this.wizardModelView.canForward()) {
      nextButton.setAction(COMMIT);
    } else {
      nextButton.setAction(FORWARD);
    }
    CardLayout layout = (CardLayout)this.wizardMainPane.getLayout();
    layout.show(this.wizardMainPane,this.wizardModelView.current().getTitle());
    System.out.println("notifyViewChange: current view " + this.wizardModelView);
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
