package com.dbn.oracleAI;

import com.dbn.common.event.ProjectEvents;
import com.dbn.common.ui.CardLayouts;
import com.dbn.common.ui.window.DBNToolWindowFactory;
import com.dbn.connection.ConsoleChangeListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.common.icon.Icons.WINDOW_DATABASE_COMPANION;
import static com.dbn.nls.NlsResources.txt;

public class OracleAIToolWindowFactory extends DBNToolWindowFactory {

  @Override
  public void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    toolWindow.setTitle(txt("companion.window.title"));
    //TODO: add header saying: Powered by Oracle's Select AI feature
    toolWindow.setStripeTitle(txt("companion.window.title"));
    toolWindow.setToHideOnEmptyContent(true);
    toolWindow.setAutoHide(false);
    toolWindow.setIcon(WINDOW_DATABASE_COMPANION.get());

    createContentPanel(toolWindow);


    DatabaseOracleAIManager manager = DatabaseOracleAIManager.getInstance(project);
    ProjectEvents.subscribe(project, manager, ConsoleChangeListener.TOPIC, connectionId -> {
      manager.switchToConnection(connectionId);
    });

  }

  private static void createContentPanel(@NotNull ToolWindow toolWindow) {
    ContentManager contentManager = toolWindow.getContentManager();
    JPanel contentPanel = CardLayouts.createCardPanel(true);

    ContentFactory contentFactory = contentManager.getFactory();
    Content content = contentFactory.createContent(contentPanel, null, true);
    contentManager.addContent(content);
  }

}
