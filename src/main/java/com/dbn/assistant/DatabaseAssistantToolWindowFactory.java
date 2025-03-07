/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.assistant;

import com.dbn.common.event.ProjectEvents;
import com.dbn.common.ui.CardLayouts;
import com.dbn.common.ui.window.DBNToolWindowFactory;
import com.dbn.common.util.Editors;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.mapping.FileConnectionContextListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;

import static com.dbn.assistant.DatabaseAssistantManager.TOOL_WINDOW_ID;
import static com.dbn.common.icon.Icons.WINDOW_DATABASE_ASSISTANT;
import static com.dbn.common.util.ContextLookup.getConnectionId;
import static com.dbn.nls.NlsResources.txt;

/**
 * Tool window factory for the Database AI-Assistant chat box
 *
 * @author Ayoub Aarrasse (Oracle)
 */
public class DatabaseAssistantToolWindowFactory extends DBNToolWindowFactory {

  @Override
  protected void initialize(@NotNull ToolWindow toolWindow) {
    toolWindow.setTitle(txt("app.assistant.title.DatabaseAssistant"));
    toolWindow.setStripeTitle(txt("app.assistant.title.DatabaseAssistant"));
    toolWindow.setIcon(WINDOW_DATABASE_ASSISTANT.get());
  }

  @Override
  public void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    createContentPanel(toolWindow);
    toolWindow.setToHideOnEmptyContent(true);
    toolWindow.setAutoHide(false);

    DatabaseAssistantManager manager = DatabaseAssistantManager.getInstance(project);

    ProjectEvents.subscribe(project, manager,
            FileConnectionContextListener.TOPIC,
            createConnectionContextListener());

    ProjectEvents.subscribe(project, manager,
            ToolWindowManagerListener.TOPIC,
            createToolWindowListener(project));
  }

  private static void createContentPanel(@NotNull ToolWindow toolWindow) {
    ContentManager contentManager = toolWindow.getContentManager();
    JPanel contentPanel = CardLayouts.createCardPanel(true);

    ContentFactory contentFactory = contentManager.getFactory();
    Content content = contentFactory.createContent(contentPanel, null, true);
    contentManager.addContent(content);
  }

  private static @NotNull FileConnectionContextListener createConnectionContextListener() {
    return new FileConnectionContextListener() {
      @Override
      public void connectionChanged(Project project, VirtualFile file, ConnectionHandler connection) {
        if (!file.isInLocalFileSystem()) return; // changing connection in surrogate (LightVirtualFiles) should not cause connection switch

        ConnectionId connectionId = connection == null ? null : connection.getConnectionId();
        DatabaseAssistantManager manager = DatabaseAssistantManager.getInstance(project);
        manager.switchToConnection(connectionId);
      }
    };
  }

  private static ToolWindowManagerListener createToolWindowListener(Project project) {
    return new ToolWindowManagerListener() {

      @Override
      public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) return;
        if (!toolWindow.isVisible()) return;

        VirtualFile file = Editors.getSelectedFile(project);
        ConnectionId connectionId = getConnectionId(project, file);
        if (connectionId == null) return; // do not switch away from last selected connection

        DatabaseAssistantManager assistantManager = DatabaseAssistantManager.getInstance(project);
        assistantManager.switchToConnection(connectionId);
      }
    };
  }

}
