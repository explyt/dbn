package com.dbn.oracleAI.config.credentials.ui;

import com.dbn.common.routine.Consumer;
import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.oracleAI.config.providers.AIProviderCredential;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CredentialPickerDialog extends DBNDialog<CredentialPickerForm> {
  private final Consumer<AIProviderCredential> callback;

  public CredentialPickerDialog(Project project, Consumer<AIProviderCredential> callback) {
    super(project, "Credential Templates", true);

    Action okAction = getOKAction();
    renameAction(okAction, "Select");
    okAction.setEnabled(false);
    this.callback = callback;
    init();
  }

  /**
   * Defines the behaviour when we click the create/update button
   * It starts by validating, and then it executes the specifies action
   */
  @Override
  protected void doOKAction() {
    callback.accept(getSelectedCredential());
    super.doOKAction();
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  @Override
  protected @NotNull CredentialPickerForm createForm() {
    return new CredentialPickerForm(this);
  }


  public void selectionChanged() {
    getOKAction().setEnabled(getSelectedCredential() != null);
  }

  private @Nullable AIProviderCredential getSelectedCredential() {
    return getForm().getSelectedCredential();
  }
}
