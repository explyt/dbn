package com.dbn.oracleAI;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.dispose.Failsafe;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

@State(name = DatabaseOracleAIManager.COMPONENT_NAME, storages = @Storage(DatabaseNavigator.STORAGE_FILE))
@Slf4j
public class DatabaseOracleAIManager extends ProjectComponentBase implements PersistentState {
  public static final String COMPONENT_NAME = "DBNavigator.Project.OracleAIManager";
  public static final String TOOL_WINDOW_ID = "Oracle Companion";
  public ConnectionId currConnection;
  private static OracleAIChatBox oracleAIChatBox;
  public final Map<ConnectionId, OracleAIChatBoxState> chatBoxStates =
      new ConcurrentHashMap<>();
  public final Map<ConnectionId, AIProfileItem> defaultProfileMap = new HashMap<>();

  private DatabaseOracleAIManager(Project project) {
    super(project, COMPONENT_NAME);
    ApplicationManager.getApplication().invokeLater(this::initOracleAIWindow);
  }

  /**
   * switch from current connection to the new selected one from DBN navigator
   *
   * @param connectionId the new selected connection
   */
  public void switchToConnection(ConnectionId connectionId) {
    assert oracleAIChatBox != null : "oracleAIChatBox not initialize";

    if (!getOracleAIWindow().isVisible()) {
      initOracleAIWindow().show(null);
    }
    // First save the current one
    if (currConnection != null) {
      OracleAIChatBoxState chatBoxState = oracleAIChatBox.captureState(currConnection.toString());
      chatBoxStates.put(currConnection, chatBoxState);
    }
    // now apply the new one
    currConnection = connectionId;
    oracleAIChatBox.setCurrentConnectionId(currConnection);
    OracleAIChatBoxState newState = chatBoxStates.get(connectionId);
    oracleAIChatBox.initState(newState, currConnection);
    oracleAIChatBox.enableWindow();
    this.getProfileService().removePropertyChangeListener(oracleAIChatBox);
    this.getProfileService().addPropertyChangeListener(oracleAIChatBox);
  }

  public ToolWindow getOracleAIWindow() {
    Project project = getProject();
    ToolWindowManager toolWindowManager =
        ToolWindowManager.getInstance(project);
    return toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
  }

  private ToolWindow initOracleAIWindow() {
    ToolWindow toolWindow = getOracleAIWindow();
    ContentManager contentManager = toolWindow.getContentManager();
    if (contentManager.getContents().length == 0) {
      oracleAIChatBox = getOracleAIChatBox();
      ContentFactory contentFactory = contentManager.getFactory();
      Content content =
          contentFactory.createContent(oracleAIChatBox, null, true);
      contentManager.addContent(content);
      toolWindow.setAvailable(true, null);
    }
    return toolWindow;
  }

  @NotNull
  public OracleAIChatBox getOracleAIChatBox() {
    return OracleAIChatBox.getInstance(getProject());
  }

  public CompletableFuture<String> queryOracleAI(String text, ActionAIType action,
                                                 String profile, String model) {
    return CompletableFuture.supplyAsync(() -> {
//      return "```\nselect something;\n```\n\nTHIS IS a response " + new Random().nextInt();
      try {
        String output;
        ConnectionHandler connection = getCurrentConnection();
        DBNConnection conn = connection.getConnection(SessionId.ORACLE_AI);
        output = connection.getOracleAIInterface()
            .executeQuery(conn, action, profile,
                text, model)
            .getQueryOutput();
        return output;
      } catch (QueryExecutionException | SQLException e) {
        throw new CompletionException(e);
      }
    });
  }

  public void openSettings() {
    AnAction action = new OracleAISettingsOpenAction(ConnectionHandler.get(currConnection));
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
    chatBoxStates.forEach((connectionId, chatBoxState) -> {
      Element chatBoxStateElement = chatBoxState.toElement();
      chatBoxStateElement.setAttribute("connectionId", connectionId.toString());
      allChatBoxStatesElement.addContent(chatBoxStateElement);
    });
    return allChatBoxStatesElement;
  }

  @Override
  public void loadComponentState(@NotNull Element element) {
    List<Element> chatBoxStateElements = element.getChildren();
    chatBoxStateElements.forEach(chatBoxStateElement -> {
      String connectionIdStr = chatBoxStateElement.getAttributeValue("connectionId");
      if (connectionIdStr != null) {
        ConnectionId connectionId = ConnectionId.get(connectionIdStr);
        OracleAIChatBoxState chatBoxState = OracleAIChatBoxState.fromElement(chatBoxStateElement);
        chatBoxStates.put(connectionId, chatBoxState);
      }
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
  public ManagedObjectServiceProxy<Profile> getProfileService() {
    return profileManagerMap.computeIfAbsent(
            getCurrentConnectionId(),
            id -> new ManagedObjectServiceProxy<>(isMockEnv() ?
                    new FakeAIProfileService() :
                    new AIProfileServiceImpl(ConnectionHandler.ensure(id))));
  }

  public AICredentialService getCredentialService() {
    return credentialManagerMap.computeIfAbsent(
            getCurrentConnectionId(),
            id -> isMockEnv() ?
                    new FakeAICredentialService() :
                    new AICredentialServiceImpl(ConnectionHandler.ensure(id)));
  }

  public DatabaseService getDatabaseService() {
    return databaseManagerMap.computeIfAbsent(
            getCurrentConnectionId(),
            id -> isMockEnv() ?
                    new FakeDatabaseService() :
                    new DatabaseServiceImpl(ConnectionHandler.ensure(id)));
  }

  private static boolean isMockEnv() {
    return Boolean.parseBoolean(System.getProperty("fake.services"));
  }

  public AIProfileItem getDefaultProfile() {
    return defaultProfileMap.get(currConnection);
  }

  public void updateDefaultProfile(AIProfileItem profile) {
    defaultProfileMap.put(currConnection, profile);
  }


  private @NotNull ConnectionHandler getCurrentConnection() {
    return ConnectionHandler.ensure(currConnection);
  }

  private @NotNull ConnectionId getCurrentConnectionId() {
    return Failsafe.nn(currConnection);
  }
}
