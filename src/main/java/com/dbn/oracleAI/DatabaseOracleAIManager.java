package com.dbn.oracleAI;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.OracleAISettingsOpenAction;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@State(name = com.dbn.debugger.DatabaseDebuggerManager.COMPONENT_NAME, storages = @Storage(DatabaseNavigator.STORAGE_FILE))
@Slf4j public class DatabaseOracleAIManager extends ProjectComponentBase
  implements PersistentState {
  public static final String COMPONENT_NAME =
    "DBNavigator.Project.OracleAIManager";
  public static final String TOOL_WINDOW_ID = "Oracle Companion";
  public ConnectionHandler currConnection;
  private static OracleAIChatBox oracleAIChatBox;
  private static volatile DatabaseOracleAIManager manager;
  private final Map<ConnectionId, OracleAIChatBoxState> chatBoxStates =
    new ConcurrentHashMap<>();

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

    // First save the current one
    if (currConnection != null) {
      chatBoxStates.put(currConnection.getConnectionId(),
                        oracleAIChatBox.captureState());
    }
    // now apply the new one
    currConnection = ConnectionHandler.get(connectionId);
    OracleAIChatBoxState newState = chatBoxStates.get(connectionId);
    if (newState != null) {
      oracleAIChatBox.restoreState(newState);
    } else {
      oracleAIChatBox.initState();
    }
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

  @NotNull public OracleAIChatBox getOracleAIChatBox() {
    return OracleAIChatBox.getInstance(getProject());
  }

  public String queryOracleAI(String text, ActionAIType action,
                              String profile) {
    String output;
    try {
      DBNConnection mainConnection =
        currConnection.getConnection(SessionId.ORACLE_AI);
      output = currConnection.getOracleAIInterface()
                             .executeQuery(mainConnection, action, profile,
                                           text)
                             .getQueryOutput();
      return output;
    } catch (SQLException e) {
      output = e.getMessage();
      System.out.println(e);
      return output;
    } catch (QueryExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * fetch all profiles
   * @return the list of profiles, can be empty not null
   */
  public List<Profile> fetchProfiles()
    throws SQLException, DatabaseOperationException {
    List<Profile> profiles = new ArrayList<>();
    if (currConnection != null) {
      DBNConnection mainConnection;

        mainConnection = currConnection.getConnection(SessionId.ORACLE_AI);
        profiles = currConnection.getOracleAIInterface().listProfiles(mainConnection);

    }
      return profiles;
  }

  public static com.dbn.oracleAI.DatabaseOracleAIManager getInstance(
    @NotNull Project project) {
    if (manager == null) {
      synchronized (DatabaseOracleAIManager.class) {
        if (manager == null) {
          manager = new DatabaseOracleAIManager(project);
        }
      }
    }
    return manager;
  }

  public void openSettings() {
    AnAction action = new OracleAISettingsOpenAction(currConnection);
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

  @Override public Element getComponentState() {
    Element allChatBoxStatesElement = new Element("OracleAIChatBoxState");
    chatBoxStates.forEach((connectionId, chatBoxState) -> {
      chatBoxState.setAttribute("connectionId", connectionId.toString());
      allChatBoxStatesElement.addContent(chatBoxState);
    });
    return allChatBoxStatesElement;
  }

  @Override public void loadComponentState(@NotNull Element element) {
    List<Element> chatBoxStateElements =
      element.getChildren("OracleAIChatBoxState");
    chatBoxStateElements.forEach(chatBoxStateElement -> {
      ConnectionId connectionId =
        ConnectionId.get(chatBoxStateElement.getAttributeValue("connectionId"));
      chatBoxStates.put(connectionId,
                        (OracleAIChatBoxState) chatBoxStateElement);
    });
  }
}
