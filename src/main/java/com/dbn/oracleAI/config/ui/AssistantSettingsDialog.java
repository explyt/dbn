package com.dbn.oracleAI.config.ui;

import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AssistantSettingsDialog extends DBNDialog<AssistantSettingsForm> {

  private final ConnectionRef connection;

  public AssistantSettingsDialog(ConnectionHandler connection) {
    super(connection.getProject(), "Database Assistant Settings", true);
    this.connection = ConnectionRef.of(connection);
    renameAction(getCancelAction(), "Close");

    setDefaultSize(800, 600);
    init();
  }

  @Override
  protected Action @NotNull [] createActions() {
    return new Action[]{getCancelAction(), new HelpAction()};
  }

  private class HelpAction extends AbstractAction {
    private HelpAction() {
      super("Help");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      AICloudSettingsForm helpDialog = new AICloudSettingsForm(getConnection());
      helpDialog.showAndGet();
    }
  }

  private ConnectionHandler getConnection() {
    return connection.ensure();
  }

  @Override
  protected @NotNull AssistantSettingsForm createForm() {
    return new AssistantSettingsForm(this, getConnection());
  }
}
