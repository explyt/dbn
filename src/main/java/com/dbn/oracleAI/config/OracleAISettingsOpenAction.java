package com.dbn.oracleAI.config;


import com.dbn.common.util.Dialogs;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.action.AbstractConnectionAction;
import com.dbn.oracleAI.config.ui.AssistantSettingsDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OracleAISettingsOpenAction extends AbstractConnectionAction {

  public OracleAISettingsOpenAction(ConnectionHandler connection) {
    super(connection);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
    Dialogs.show(() -> new AssistantSettingsDialog(connection));
  }
}


