package com.dbn.oracleAI.config.credentials.ui;

import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.types.CredentialType;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

public class CredentialEditDialog extends DBNDialog<CredentialEditForm> {

  private final ConnectionRef connection;
  private final Credential credential;
  private final Runnable callback;
  private final Set<String> usedCredentialNames;
  private CredentialEditFormValidator validator;


  public CredentialEditDialog(ConnectionHandler connection, @Nullable Credential credential, @NotNull Set<String> usedCredentialNames, Runnable callback) {
    super(connection.getProject(), getDialogTitle(credential), true);
    this.connection = ConnectionRef.of(connection);
    this.credential = credential;
    this.usedCredentialNames = usedCredentialNames;
    this.callback = callback;

    init();
  }

  private static String getDialogTitle(@Nullable Credential credential) {
    return credential == null ? "Create Credential" : "Update Credential";
  }

  /**
   * Defines the validation logic for the fields
   */
  @Override
  protected ValidationInfo doValidate() {
    return validator.validate();
  }

  /**
   * Defines the behaviour when we click the create/update button
   * It starts by validating, and then it executes the specifies action
   */
  @Override
  protected void doOKAction() {
    CredentialEditForm form = getForm();
    if (form.getSaveInfoCheckBox().isSelected() && form.getCredentialTypeComboBox().getSelectedItem() == CredentialType.PASSWORD) {
      form.saveProviderInfo();
    }
    if (credential != null) {
      form.doUpdateAction();
    } else {
      form.doCreateAction();
    }
    super.doOKAction();
    callback.run();
  }

  @Override
  protected Action @NotNull [] createActions() {
    super.setOKButtonText(txt(credential != null ? "ai.messages.button.update" : "ai.messages.button.create"));
    return super.createActions();
  }

  private ConnectionHandler getConnection() {
    return connection.ensure();
  }

  @Override
  protected @NotNull CredentialEditForm createForm() {
    CredentialEditForm form = new CredentialEditForm(getConnection(), credential, usedCredentialNames);
    validator = new CredentialEditFormValidator(form);
    return form;
  }


}
