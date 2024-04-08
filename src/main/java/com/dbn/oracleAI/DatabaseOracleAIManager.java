package com.dbn.oracleAI;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.SessionId;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.OracleAISettingsOpenAction;
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
import java.util.concurrent.ConcurrentHashMap;

@State(name = DatabaseOracleAIManager.COMPONENT_NAME, storages = @Storage(DatabaseNavigator.STORAGE_FILE))
@Slf4j public class DatabaseOracleAIManager extends ProjectComponentBase
  implements PersistentState {
  public static final String COMPONENT_NAME =
    "DBNavigator.Project.OracleAIManager";
  public static final String TOOL_WINDOW_ID = "Oracle Companion";
  public ConnectionId currConnection;
  private static OracleAIChatBox oracleAIChatBox;
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
      chatBoxStates.put(currConnection,
                        oracleAIChatBox.captureState(currConnection.toString()));
    }
    // now apply the new one
    currConnection = connectionId;
    OracleAIChatBoxState newState = chatBoxStates.get(connectionId);
      oracleAIChatBox.initState();
    if (newState != null) {
      oracleAIChatBox.restoreState(newState);
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
        Objects.requireNonNull(ConnectionHandler.get(currConnection)).getConnection(SessionId.ORACLE_AI);
      output = Objects.requireNonNull(ConnectionHandler.get(currConnection)).getOracleAIInterface()
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

  @Override public Element getComponentState() {
    Element allChatBoxStatesElement = new Element("OracleAIChatBoxStates");
    chatBoxStates.forEach((connectionId, chatBoxState) -> {
      Element chatBoxStateElement = chatBoxState.toElement();
      chatBoxStateElement.setAttribute("connectionId", connectionId.toString());
      allChatBoxStatesElement.addContent(chatBoxStateElement);
    });
    return allChatBoxStatesElement;
  }

  @Override public void loadComponentState(@NotNull Element element) {
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


  //internal map hosting references to manager
  //TODO : hook to connection deletion to cleanup map
  // we assume non static map as we have only one instance fo this
  private Map<ConnectionId,AIProfileService>profileManagerMap = new HashMap<>();

  private Map<ConnectionId,AICredentialService>credentialManagerMap = new HashMap<>();


  /**
   * Gets a profile manager for the current connection.
   * Managers are sigletons
   * Ww assume that we alwasy have a current connection
   * @return a manager.
   */
  public synchronized AIProfileService getProfileService() {
    //TODO : later find better than using "synchronized"
    return profileManagerMap.getOrDefault(ConnectionHandler.get(currConnection).getConnectionId(),
                                          new AIProfileService(ConnectionHandler.get(currConnection)));
  }
  public synchronized AICredentialService getCredentialService() {
    //TODO : later find better than using "synchronized"
    return credentialManagerMap.getOrDefault(ConnectionHandler.get(currConnection).getConnectionId(),
                                          new AICredentialService(ConnectionHandler.get(currConnection)));
  }

}
