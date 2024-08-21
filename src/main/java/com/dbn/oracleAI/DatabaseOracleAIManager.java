package com.dbn.oracleAI;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.database.DatabaseFeature;
import com.dbn.oracleAI.config.OracleAISettingsOpenAction;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.ui.OracleAIChatBox;
import com.dbn.oracleAI.ui.OracleAIChatBoxState;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
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
import static com.dbn.common.options.setting.Settings.connectionIdAttribute;
import static com.dbn.common.ui.CardLayouts.*;

@State(name = DatabaseOracleAIManager.COMPONENT_NAME, storages = @Storage(DatabaseNavigator.STORAGE_FILE))
@Slf4j
public class DatabaseOracleAIManager extends ProjectComponentBase implements PersistentState {
  public static final String COMPONENT_NAME = "DBNavigator.Project.OracleAIManager";
  public static final String TOOL_WINDOW_ID = "Oracle Companion";

  private final Map<ConnectionId, OracleAIChatBox> chatBoxes = new ConcurrentHashMap<>();
  private final Map<ConnectionId, AIProfileItem> defaultProfileMap = new HashMap<>();

  private DatabaseOracleAIManager(Project project) {
    super(project, COMPONENT_NAME);
    //ApplicationManager.getApplication().invokeLater(this::initOracleAIWindow);
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
    captureState(selectedConnectionId);

    initToolWindow(connectionId);
  }

  public void captureState(ConnectionId connectionId) {
    OracleAIChatBox chatBox = getChatBox(connectionId);
    if (chatBox != null) chatBox.captureState();
  }

  @Nullable
  private OracleAIChatBox getChatBox(ConnectionId connectionId) {
    if (connectionId == null) return null;

    ConnectionHandler connection = ConnectionHandler.get(connectionId);
    if (connection == null) return null;

    if (!DatabaseFeature.AI_ASSISTANT.isSupported(connection)) return null;

    return chatBoxes.computeIfAbsent(connectionId, id -> {
      OracleAIChatBox chatBox = new OracleAIChatBox(connection);
      chatBox.initState(null);
      chatBox.enableWindow();

      addCard(getToolWindowPanel(), chatBox, connectionId);
      return chatBox;
    });
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

  public CompletableFuture<String> queryOracleAI(ConnectionId connectionId, String text, ActionAIType action,
                                                 String profile, String model) {
    return CompletableFuture.supplyAsync(() -> {
//      return "```\nselect something;\n```\n\nTHIS IS a response " + new Random().nextInt();
      try {
        ConnectionHandler connection = ConnectionHandler.ensure(connectionId);
        DBNConnection conn = connection.getConnection(SessionId.ORACLE_AI);
        return connection.getOracleAIInterface()
                .executeQuery(conn, action, profile, text, model)
                .getQueryOutput();
      } catch (QueryExecutionException | SQLException e) {
        throw new CompletionException(e);
      }
    });
  }

  public void openSettings(ConnectionId connectionId) {
    AnAction action = new OracleAISettingsOpenAction(ConnectionHandler.get(connectionId));
    AnActionEvent event =
        AnActionEvent.createFromDataContext(ActionPlaces.UNKNOWN, null,
            dataId -> {
              if (PlatformDataKeys.PROJECT.is(
                  dataId)) {
                return getProject();
              }
              return null;
            });
    action.actionPerformed(event);
  }

  /*********************************************
   *            PersistentStateComponent       *
   *********************************************/

  @Override
  public Element getComponentState() {
    Element allChatBoxStatesElement = new Element("OracleAIChatBoxStates");
    chatBoxes.forEach((connectionId, chatBox) -> {
      Element chatBoxStateElement = chatBox.getState().toElement();
      chatBoxStateElement.setAttribute("connectionId", connectionId.toString());
      allChatBoxStatesElement.addContent(chatBoxStateElement);
    });
    return allChatBoxStatesElement;
  }

  @Override
  public void loadComponentState(@NotNull Element element) {
    List<Element> chatBoxStateElements = element.getChildren();
    chatBoxStateElements.forEach(chatBoxStateElement -> {
      ConnectionId connectionId = connectionIdAttribute(chatBoxStateElement, "connectionId");

      OracleAIChatBoxState chatBoxState = OracleAIChatBoxState.fromElement(chatBoxStateElement);
      OracleAIChatBox chatBoxForm = getChatBox(connectionId);
      chatBoxForm.setState(chatBoxState);
    });
  }

  private final Map<ConnectionId, ManagedObjectServiceProxy<Profile>> profileManagerMap = new ConcurrentHashMap<>();

  private final Map<ConnectionId, AICredentialService> credentialManagerMap = new ConcurrentHashMap<>();

  private final Map<ConnectionId, DatabaseService> databaseManagerMap = new ConcurrentHashMap<>();


  /**
   * Gets a profile manager for the current connection.
   * Managers are singletons
   * Ww assume that we always have a current connection
   *
   * @return a manager.
   */
  public ManagedObjectServiceProxy<Profile> getProfileService(ConnectionId connectionId) {
    return profileManagerMap.computeIfAbsent(
            connectionId,
            id -> new ManagedObjectServiceProxy<>(isMockEnv() ?
                    new FakeAIProfileService() :
                    new AIProfileServiceImpl(ConnectionHandler.ensure(id))));
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
