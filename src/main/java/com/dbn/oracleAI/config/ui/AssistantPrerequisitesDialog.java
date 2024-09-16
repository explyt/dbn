package com.dbn.oracleAI.config.ui;

import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AssistantPrerequisitesDialog extends DBNDialog<AssistantPrerequisiteForm> {

  private final ConnectionRef connection;

  public AssistantPrerequisitesDialog(ConnectionHandler connection) {
    super(connection.getProject(), "Select AI Help", true);
    this.connection = ConnectionRef.of(connection);
    renameAction(getCancelAction(), "Close");

    setResizable(false);
    init();
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getCancelAction()};
  }

  public ConnectionHandler getConnection() {
    return connection.ensure();
  }

  @Override
  protected @NotNull AssistantPrerequisiteForm createForm() {
    return new AssistantPrerequisiteForm(this);
  }
}
