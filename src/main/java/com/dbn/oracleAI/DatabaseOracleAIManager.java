/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.config.ui.OracleAISettingsWindow;
import com.dbn.oracleAI.ui.ChatBoxState;
import com.dbn.oracleAI.ui.OracleAIChatBox;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

import static com.dbn.common.component.Components.projectService;
import static com.dbn.common.options.setting.Settings.newElement;
import static com.dbn.common.ui.CardLayouts.*;

/**
 * Main database AI-Assistance management component
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 * @author Emmanuel Jannetti (emmanuel.jannetti@oracle.com)
 */
@Slf4j
@State(
    name = DatabaseOracleAIManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE))
public class DatabaseOracleAIManager extends ProjectComponentBase implements PersistentState {
  public static final String COMPONENT_NAME = "DBNavigator.Project.DatabaseAssistantManager";
  public static final String TOOL_WINDOW_ID = "Oracle Companion";

  private final Map<ConnectionId, ChatBoxState> chatStates = new ConcurrentHashMap<>();
  private final Map<ConnectionId, OracleAIChatBox> chatBoxes = new ConcurrentHashMap<>();
  private final Map<ConnectionId, AIProfileItem> defaultProfileMap = new HashMap<>();

  private DatabaseOracleAIManager(Project project) {
    super(project, COMPONENT_NAME);
  }

  public static DatabaseOracleAIManager getInstance(@NotNull Project project) {
    return projectService(project, DatabaseOracleAIManager.class);
  }

  /**
   * switch from current connection to the new selected one from DBN navigator
   *
   * @param connectionId the new selected connection
   */
  public void switchToConnection(@Nullable ConnectionId connectionId) {
    JPanel toolWindowPanel = getToolWindowPanel();
    String id = visibleCardId(toolWindowPanel);
    ConnectionId selectedConnectionId = isBlankCard(id) ? null : ConnectionId.get(id);

    if (Objects.equals(selectedConnectionId, connectionId)) return;
    initToolWindow(connectionId);
  }

  @Nullable
  private OracleAIChatBox getChatBox(@Nullable ConnectionId connectionId) {
    if (connectionId == null) return null;

    ConnectionHandler connection = ConnectionHandler.get(connectionId);
    if (connection == null) return null;
    // TODO clarify - present assistant for unsupported databases?
    //if (!AI_ASSISTANT.isSupported(connection)) return null;

    return chatBoxes.computeIfAbsent(connectionId, id -> {
      OracleAIChatBox chatBox = new OracleAIChatBox(connection);
      addCard(getToolWindowPanel(), chatBox, connectionId);
      return chatBox;
    });
  }

  public ChatBoxState getChatBoxState(ConnectionId connectionId, boolean ensure) {
    return chatStates.compute(connectionId, (c, s) -> s == null && ensure ? new ChatBoxState(c) : s);
  }

  public ToolWindow getToolWindow() {
    ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
    return toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
  }

  private JPanel getToolWindowPanel() {
    Content content = getToolWindow().getContentManager().getContent(0);
    return (JPanel) content.getComponent();
  }

  private void initToolWindow(ConnectionId connectionId) {
    ToolWindow toolWindow = getToolWindow();
    JPanel toolWindowPanel = getToolWindowPanel();

    OracleAIChatBox chatBox = getChatBox(connectionId);
    if (chatBox == null) {
      showBlankCard(toolWindowPanel);
    } else {
      showCard(toolWindowPanel, connectionId);
      toolWindow.setAvailable(true);
    }
  }

  public CompletableFuture<String> queryOracleAI(ConnectionId connectionId, String text, String action,
                                                 String profile, String model) {
    return CompletableFuture.supplyAsync(() -> {
//      return "```\nselect something;\n```\n\nTHIS IS a response " + new Random().nextInt();
      try {
        ConnectionHandler connection = ConnectionHandler.ensure(connectionId);
        DBNConnection conn = connection.getConnection(SessionId.ORACLE_AI);
        return connection.getAssistantInterface()
                .executeQuery(conn, action, profile, text, model)
                .getQueryOutput();
      } catch (QueryExecutionException | SQLException e) {
        throw new CompletionException(e);
      }
    });
  }

  public void openSettings(ConnectionHandler connection) {
    OracleAISettingsWindow settingsWindow = new OracleAISettingsWindow(connection);
    settingsWindow.display();
  }

  /*********************************************
   *            PersistentStateComponent       *
   *********************************************/

  @Override
  public Element getComponentState() {
    Element element = newElement("state");
    Element statesElement = newElement(element, "chat-box-states");
    for (ConnectionId connectionId : chatStates.keySet()) {
      ChatBoxState state = chatStates.get(connectionId);
      Element stateElement = newElement(statesElement, "chat-box-state");
      state.writeState(stateElement);
    }
    return element;
  }

  @Override
  public void loadComponentState(@NotNull Element element) {
    Element statesElement = element.getChild("chat-box-states");
    if (statesElement != null) {
      List<Element> stateElements = statesElement.getChildren();
      for (Element stateElement : stateElements) {
        ChatBoxState state = new ChatBoxState();
        state.readState(stateElement);
        chatStates.put(state.getConnectionId(), state);
      }
    }
  }

  private final Map<ConnectionId, AIProfileService> profileManagerMap = new ConcurrentHashMap<>();

  private final Map<ConnectionId, AICredentialService> credentialManagerMap = new ConcurrentHashMap<>();

  private final Map<ConnectionId, DatabaseService> databaseManagerMap = new ConcurrentHashMap<>();


  /**
   * Gets a profile manager for the current connection.
   * Managers are singletons
   * Ww assume that we always have a current connection
   *
   * @return a manager.
   */
  public AIProfileService getProfileService(ConnectionId connectionId) {
    return profileManagerMap.computeIfAbsent(
            connectionId,
            id -> new AIProfileService.CachedProxy((isMockEnv() ?
                    new FakeAIProfileService() :
                    new AIProfileServiceImpl(ConnectionHandler.ensure(id)))));
  }

  public AICredentialService getCredentialService(ConnectionId connectionId) {
    return credentialManagerMap.computeIfAbsent(connectionId, id ->
            isMockEnv() ?
                    new FakeAICredentialService() :
                    new AICredentialServiceImpl(ConnectionHandler.ensure(id)));
  }

  public DatabaseService getDatabaseService(ConnectionId connectionId) {
    return databaseManagerMap.computeIfAbsent(connectionId, id ->
            isMockEnv() ?
                    new FakeDatabaseService() :
                    new DatabaseServiceImpl(ConnectionHandler.ensure(id)));
  }

  private static boolean isMockEnv() {
    return Boolean.parseBoolean(System.getProperty("fake.services"));
  }

  public AIProfileItem getDefaultProfile(ConnectionId connectionId) {
    return defaultProfileMap.get(connectionId);
  }

  public void updateDefaultProfile(ConnectionId connectionId, AIProfileItem profile) {
    defaultProfileMap.put(connectionId, profile);
  }

}
