package com.dbn.oracleAI;

import com.dbn.common.event.ProjectEvents;
import com.dbn.common.icon.Icons;
import com.dbn.connection.ConsoleChangeListener;
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
    toolWindow.setStripeTitle(messages.getString("companion.window.title"));
    toolWindow.setIcon(Icons.WINDOW_DATABASE_COMPANION);
    toolWindow.setToHideOnEmptyContent(true);
    toolWindow.setAutoHide(false);
    DatabaseOracleAIManager manager = project.getService(DatabaseOracleAIManager.class);
    ProjectEvents.subscribe(project, manager, ConsoleChangeListener.TOPIC, manager::switchToConnection);

  }
}
