package com.dbn.oracleAI;

import com.dbn.common.event.ProjectEvents;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConsoleChangeListener;
import com.dbn.connection.DatabaseType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public class OracleAIToolWindowFactory implements ToolWindowFactory, DumbAware {
  private final static ResourceBundle messages = ResourceBundle.getBundle("Messages", Locale.getDefault());

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    toolWindow.setTitle(messages.getString("companion.window.title"));
    //TODO: add header saying: Powered by Oracle's Select AI feature
    toolWindow.setStripeTitle(messages.getString("companion.window.title"));
    toolWindow.setToHideOnEmptyContent(true);
    toolWindow.setAutoHide(false);
    DatabaseOracleAIManager manager = project.getService(DatabaseOracleAIManager.class);
    ProjectEvents.subscribe(project, manager, ConsoleChangeListener.TOPIC, connectionId -> {
      ConnectionHandler connectionHandler = ConnectionHandler.get(connectionId);
      // TODO: filter only oracledb version that are supported for this feature Select AI feature
      if (connectionHandler != null && connectionHandler.getDatabaseType() == DatabaseType.ORACLE) {
        manager.switchToConnection(connectionId);
      } else {
        ApplicationManager.getApplication().invokeLater(() -> {
          toolWindow.getContentManager().removeAllContents(false);
          toolWindow.setAvailable(false, null);
        });

      }
    });

  }

}
