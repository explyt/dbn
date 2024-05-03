package com.dbn.oracleAI.config;


import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.action.AbstractConnectionAction;
import com.dbn.oracleAI.config.ui.OracleAISettingsWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OracleAISettingsOpenAction extends AbstractConnectionAction {

  public OracleAISettingsOpenAction(ConnectionHandler connection) {
    super(connection);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ConnectionHandler connection) {
    OracleAISettingsWindow settingsWindow = new OracleAISettingsWindow(connection);
    settingsWindow.display();
  }
}


