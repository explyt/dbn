package com.dbn.oracleAI.ui;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionId;
import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.types.AuthorType;
import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.utils.RollingJPanelWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.awt.event.InputEvent.BUTTON1_MASK;

/**
 * Holder class for profile Combox box
 */
public class OracleAIChatBox extends JPanel implements PropertyChangeListener {


  private static final Logger LOG = Logger.getInstance(OracleAIChatBox.class.getPackageName());

  private static OracleAIChatBox instance;
  private ConnectionId currentConnectionId;
  private JComboBox<AIProfileItem> profileComboBox;
  private ProfileComboBoxModel profileListModel = new ProfileComboBoxModel();
  private JPanel chatBoxMainPanel;
  private JCheckBox explainSQLCheckbox;
  private JPanel companionConversationPanel;
  private JPanel conversationPanel;
  private RollingJPanelWrapper conversationPanelWrapper;
  private JScrollPane companionConversationScrollPan;
  private JPanel companionCommandPanel;
  private JComboBox<ProviderModel> aiModelComboBox;
  private JPanel companionConversationPanelTop;
  private JProgressBar activityProgress;
  private JTextField chatBoxNotificationMessage;
  private JPanel companionConversationPanelBottom;
  private JButton promptButton;
  private JTextArea promptTextArea;
  private JPanel buttonPanel;
  private JButton clearAllMessages;

  public static DatabaseOracleAIManager currManager;

  static private final ResourceBundle messages =
      ResourceBundle.getBundle("Messages", Locale.getDefault());

  /**
   * special profile combobox item that lead to create a new profile
   */
  static final AIProfileItem ADD_PROFILE_COMBO_ITEM =
      new AIProfileItem(messages.getString("companion.profile.combobox.add"), false);


  private OracleAIChatBox() {
    profileListModel = new ProfileComboBoxModel();
    initializeUI();
    this.setLayout(new BorderLayout(1, 1));
    this.add(chatBoxMainPanel, BorderLayout.CENTER);
  }

  private void createUIComponents() {
    promptTextArea = new IdleJtextArea(messages.getString("companion.chat.prompt.tooltip"));
    activityProgress = new ActivityNotifier();

  }


  /**
   * Dedicated class for NL2SQL profile model.
   */
  class ProfileComboBoxModel extends DefaultComboBoxModel<AIProfileItem> {

    @Override
    public void removeAllElements() {
      profileComboBox.removeActionListener(profileActionListener);
      super.removeAllElements();
      profileComboBox.addActionListener(profileActionListener);

    }

    /**
     * Gets the list of labels
     *
     * @return all item labels i.e profile names
     */
    public List<AIProfileItem> getAllProfiles() {
      List<AIProfileItem> result = new ArrayList<>();
      for (int i = 0; i < this.getSize() - 1; i++) {
        result.add(this.getElementAt(i));
      }
      return result;
    }

    /**
     * Get list of effective profiles.
     * remove non-effective profiles and the special
     *
     * @return
     */
    public List<AIProfileItem> getUsableProfiles() {
      return this.getAllProfiles().stream().filter(AIProfileItem::isEffective).collect(
          Collectors.toList());
    }
  }


  public static OracleAIChatBox getInstance(Project project) {
    currManager = project.getService(DatabaseOracleAIManager.class);
    if (instance == null) {
      instance = new OracleAIChatBox();
    }
    return instance;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("com.dbn.oracleAI.AIProfileServiceImpl".equals(evt.getPropertyName())) {
      updateProfileComboBox();
    }
  }

  private void updateProfileComboBox() {
    startActivityNotifier(messages.getString("companion.chat.fetching_profiles"));
    updateProfiles().thenAccept(finalFetchedProfiles -> {
      LOG.debug(finalFetchedProfiles.size() + " Profiles fetched successfully");
      ApplicationManager.getApplication().invokeLater(() -> {
        profileListModel.removeAllElements();
        finalFetchedProfiles.forEach((pn, p) -> {
          profileListModel.addElement(new AIProfileItem(pn, p.getProvider(), p.getModel(), p.isEnabled()));
        });

        profileListModel.addElement(ADD_PROFILE_COMBO_ITEM);
        if (profileListModel.getUsableProfiles().isEmpty()) {
          disableWindow("companion.chat.no_profile_yet.tooltip");
        } else {
          enableWindow();
        }
        AIProfileItem currProfileItem = (AIProfileItem) profileComboBox.getSelectedItem();
        if (currProfileItem != null && currProfileItem.getProvider() != null) updateModelsComboBox(currProfileItem);
        stopActivityNotifier();
      });

    }).exceptionally(e -> {
      LOG.warn("Failed to fetch profiles", e);
      ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currManager.getProject(), e.getCause().getMessage()));
      stopActivityNotifier();
      return null;
    });
  }

  public void setCurrentConnectionId(ConnectionId connectionId) {
    this.currentConnectionId = connectionId;
    if (currentConnectionId == null) disableWindow("companion.chat.wrong_database_type.tooltip");
  }

  private void initializeUI() {
    disableWindow("companion.chat.no_console.tooltip");
    configureChatHeaderPanel();
    LOG.info("Header of chat window displayed");
    configureConversationPanel();
    LOG.info("Center of chat window displayed");
    configurePromptArea();
    LOG.info("Bottom of chat window displayed");
  }

  /**
   * Initializes the list of profiles available, models available, and the checkbox for explain sql
   */
  private void configureChatHeaderPanel() {
    explainSQLCheckbox.setText(messages.getString("companion.explainSql.action"));

    profileComboBox.setModel(profileListModel);

    profileComboBox.addActionListener(profileActionListener);
    profileComboBox.setRenderer(new ProfileComboBoxRenderer());

    clearAllMessages.setIcon(Icons.ACTION_DELETE);
    clearAllMessages.addActionListener(e -> {
      conversationPanelWrapper.clear();
    });

  }

  private ActionListener profileActionListener = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      AIProfileItem currProfileItem = (AIProfileItem) profileComboBox.getSelectedItem();
      if (Objects.equals(currProfileItem, ADD_PROFILE_COMBO_ITEM)) {
        if (e.getModifiers() == BUTTON1_MASK) {
          profileComboBox.hidePopup();
          profileComboBox.setSelectedIndex(0);
          currManager.openSettings();
        }
      } else {
        updateModelsComboBox(currProfileItem);
      }

      if (!currProfileItem.isEnabled()) {
        SwingUtilities.invokeLater(() -> {
          disableWindow("companion.chat.disabled_profile.tooltip");
        });
      } else {
        enableWindow();
      }
    }
  };

  private class ProfileComboBoxRenderer extends BasicComboBoxRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected,
          cellHasFocus);
      if (ADD_PROFILE_COMBO_ITEM.equals(value)) {
        setFont(getFont().deriveFont(Font.ITALIC));
      }
      if (value instanceof AIProfileItem) {
        AIProfileItem item = (AIProfileItem) value;
        setEnabled(item.isEnabled());
      }
      return this;
    }
  }

  /**
   * Initializes the panel to display messages
   */
  private void configureConversationPanel() {
    conversationPanel.setLayout(new MigLayout("fillx"));

    companionConversationScrollPan.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    companionConversationScrollPan.add(conversationPanel);
    companionConversationScrollPan.setViewportView(conversationPanel);

    conversationPanelWrapper = new RollingJPanelWrapper(OracleAIChatBoxState.MAX_CHAR_MESSAGE_COUNT,
        conversationPanel);

  }

  /**
   * Initializes the prompt panel, with a text area and a button
   * Adding event listeners
   */
  private void configurePromptArea() {
    promptButton.setIcon(Icons.ACTION_EXECUTE);
    // TODO : fine better than this one...
    promptButton.setDisabledIcon(Icons.CONNECTION_DISABLED);
    promptButton.setToolTipText(messages.getString("companion.chat.prompt.execute.tooltip"));
    promptButton.setBorder(BorderFactory.createEmptyBorder());
    promptButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

    companionConversationPanelBottom.setBackground(promptTextArea.getBackground());
    buttonPanel.setBackground(promptTextArea.getBackground());
    promptButton.addActionListener(e -> {
      submitText();
    });

    InputMap inputMap = promptTextArea.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap actionMap = promptTextArea.getActionMap();

    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "submit");
    inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");

    actionMap.put("submit", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!((IdleJtextArea) promptTextArea).isIdle() &&
            !promptTextArea.getText().isEmpty()) {
          submitText();
        }
      }
    });
    promptTextArea.addFocusListener(new FocusListener() {
                                      @Override
                                      public void focusGained(FocusEvent e) {
                                        // nothing to do
                                      }

                                      @Override
                                      public void focusLost(FocusEvent e) {
                                        // if only blanks or is idle
                                        // forbid to submit
                                        if (promptTextArea.getText().isEmpty() ||
                                            ((IdleJtextArea) promptTextArea).isIdle()) {
                                          LOG.debug("focusLost disabling submit");
                                          promptButton.setEnabled(false);
                                        } else {
                                          LOG.debug("focusLost enabling submit");
                                          promptButton.setEnabled(true);
                                        }
                                      }
                                    }
    );

  }


  private void submitText() {
    startActivityNotifier(messages.getString("companion.chat.submitting"));
    ApplicationManager.getApplication()
        .executeOnPooledThread(
            () -> {
              // we know that this is never empty and good to be proceed
              String question = promptTextArea.getText();
              ((IdleJtextArea) promptTextArea).setIdleMode(true);
              promptTextArea.setEnabled(false);
              processQuery(question, selectedAction());
            });
  }

  private ActionAIType selectedAction() {
    if (explainSQLCheckbox.isSelected()) {
      return ActionAIType.EXPLAINSQL;
    } else {
      return ActionAIType.SHOWSQL;
    }
  }

  private void processQuery(String question, ActionAIType actionType) {
    LOG.debug("Starting processQuery with actionType: " + actionType);
    Objects.requireNonNull(promptTextArea.getText(),
        "cannot be here without question been selected");

    AIProfileItem item = (AIProfileItem) profileComboBox.getSelectedItem();

    // TODO : do we want to append the message even in case of error
    ChatMessage inputChatMessage = new ChatMessage(question, AuthorType.USER);
    appendMessageToChat(List.of(inputChatMessage));
    currManager.queryOracleAI(question, actionType, item.getLabel(),
            ((ProviderModel) aiModelComboBox.getSelectedItem()).getApiName())
        .thenAccept((output) -> {
          ChatMessage outPutChatMessage = new ChatMessage(output, AuthorType.AI);
          appendMessageToChat(List.of(outPutChatMessage));
          LOG.debug("Query processed successfully.");
        })
        .exceptionally(e -> {
          LOG.warn("Error processing query", e);
          ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currManager.getProject(), e.getMessage()));
          return null;
        })
        .thenRun(() -> {
          stopActivityNotifier();
          promptTextArea.setEnabled(true);
        });
  }


  /**
   * Updates profile combobox box model by fetching
   * list of available profiles for the current connection
   */
  public CompletableFuture<Map<String, Profile>> updateProfiles() {
    return currManager.getProfileService().list().thenApply(pl -> pl.stream()
        .collect(Collectors.toMap(Profile::getProfileName,
            Function.identity(),
            (existing, replacement) -> existing)));
  }

  /**
   * Updates profile combobox box model with new profile item list
   */
  public void updateProfiles(List<AIProfileItem> items) {
    profileListModel.removeAllElements();
    profileListModel.addAll(items);
    profileListModel.addElement(ADD_PROFILE_COMBO_ITEM);
    profileListModel.setSelectedItem(profileComboBox.getItemAt(0));
  }

  /**
   * Backup current elements state for later re-use
   * when we're going to enter here again with a new connection
   *
   * @return the state for this current companion
   */
  public OracleAIChatBoxState captureState(String currConnection) {
    AIProfileItem selectedProfile = (AIProfileItem) profileListModel.getSelectedItem();
    return OracleAIChatBoxState.builder()
        .currConnection(currConnection)
        .aiAnswers(conversationPanelWrapper.getMessages())
        .profiles(new ArrayList<>(profileListModel.getAllProfiles()))
        .currentQuestionText(promptTextArea.getText())
        .selectedProfile((selectedProfile != null && selectedProfile.isEffective()) ? selectedProfile : null)
        .build();
  }

  /**
   * Restores the element state according to the current connection.
   *
   * @param state the state that should be applied
   */
  public void restoreState(OracleAIChatBoxState state) {
    LOG.debug("Restore State");
    assert state != null : "cannot be null";
    this.updateProfiles(state.getProfiles());
    if (state.getSelectedProfile() != null) profileComboBox.setSelectedItem(state.getSelectedProfile());
    conversationPanelWrapper.clear();
    appendMessageToChat(state.getAiAnswers());

    promptTextArea.setText(state.getCurrentQuestionText());
  }

  /**
   * Starts the spinning wheel
   * TODO : try to be clever here...
   */
  private void startActivityNotifier(String message) {
    ((ActivityNotifier) activityProgress).start();
    chatBoxNotificationMessage.setVisible(true);
    chatBoxNotificationMessage.setText(message);
  }

  /**
   * Stops the spinning wheel
   */
  private void stopActivityNotifier() {
    ((ActivityNotifier) activityProgress).stop();

    chatBoxNotificationMessage.setVisible(false);
    chatBoxNotificationMessage.setText("");

  }

  /**
   * Restores the element state from scratch.
   * basically called only once ny switchConnection()
   */
  public void initState(@Nullable OracleAIChatBoxState newState, ConnectionId connectionId) {
    LOG.debug("Initialize new state");
    if (newState != null) restoreState(newState);
    startActivityNotifier(messages.getString("companion.chat.fetching_profiles"));
    updateProfiles().thenAccept(finalFetchedProfiles -> {
      if (connectionId == currentConnectionId) {
        LOG.debug(finalFetchedProfiles.size() + " Profiles fetched successfully");
        ApplicationManager.getApplication().invokeLater(() -> {
          profileListModel.removeAllElements();
//          currManager.getProfileService().updateCachedProfiles(new ArrayList<>(finalFetchedProfiles.values()));
          finalFetchedProfiles.forEach((pn, p) -> {
            profileListModel.addElement(new AIProfileItem(pn, p.getProvider(), p.getModel(), p.isEnabled()));
          });

          profileListModel.addElement(ADD_PROFILE_COMBO_ITEM);
          if (newState != null && profileListModel.getAllProfiles().contains(newState.getSelectedProfile()))
            profileListModel.setSelectedItem(newState.getSelectedProfile());

          if (profileListModel.getUsableProfiles().isEmpty()) {
            disableWindow("companion.chat.no_profile_yet.tooltip");
          } else {
            enableWindow();
          }
          AIProfileItem currProfileItem = (AIProfileItem) profileComboBox.getSelectedItem();
          if (currManager.getDefaultProfile() == null) {
            currManager.updateDefaultProfile(currProfileItem);
          }
          if (currProfileItem != null && currProfileItem.getProvider() != null) updateModelsComboBox(currProfileItem);
          stopActivityNotifier();
        });
      }
    }).exceptionally(e -> {
      LOG.warn("Failed to fetch profiles", e);
      ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currManager.getProject(), e.getCause().getMessage()));
      stopActivityNotifier();
      return null;
    });
  }

  public void enableWindow() {
    explainSQLCheckbox.setEnabled(true);
    promptTextArea.setEnabled(true);
    promptButton.setEnabled(true);
    aiModelComboBox.setEnabled(true);
    profileComboBox.setEnabled(true);
    companionConversationScrollPan.setEnabled(true);
    aiModelComboBox.setToolTipText("companion.chat.model.tooltip");
    explainSQLCheckbox.setToolTipText(
        messages.getString("companion.explainsql.tooltip"));
    promptTextArea.setToolTipText("");
    profileComboBox.setToolTipText(messages.getString("companion.chat.profile.tooltip"));
    companionConversationScrollPan.setToolTipText("");
    promptButton.setToolTipText(messages.getString("companion.chat.prompt.button.tooltip"));
  }

  private void disableWindow(String message) {
    explainSQLCheckbox.setEnabled(false);
    promptTextArea.setEnabled(false);
    promptButton.setEnabled(false);
    aiModelComboBox.setEnabled(false);
    companionConversationScrollPan.setEnabled(false);
    explainSQLCheckbox.setToolTipText(
        messages.getString(message));
    aiModelComboBox.setToolTipText(messages.getString(message));
    promptTextArea.setToolTipText(
        messages.getString(message));
    promptButton.setToolTipText(
        messages.getString(message));
    companionConversationScrollPan.setToolTipText(messages.getString(message));
    if (message.equals("companion.chat.no_console.tooltip") || message.equals("companion.chat.wrong_database_type.tooltip")) {
      profileComboBox.setEnabled(false);
      profileComboBox.setToolTipText(messages.getString(message));
    }
  }

  private void updateModelsComboBox(AIProfileItem currProfileItem) {
    aiModelComboBox.removeAllItems();
    if (currProfileItem.getProvider() != null) {
      for (ProviderModel model : currProfileItem.getProvider().getModels()) {
        aiModelComboBox.addItem(model);
      }
    }
    if (currProfileItem.getModel() != null) {
      aiModelComboBox.setSelectedItem(currProfileItem.getModel());
    } else {
      if (currProfileItem.getProvider() != null) {
        // select the default
        aiModelComboBox.setSelectedItem(currProfileItem.getProvider().getDefaultModel());
      } else {
        aiModelComboBox.setSelectedItem(aiModelComboBox.getItemAt(0));
      }
    }
  }

  private void appendMessageToChat(List<ChatMessage> chatMessages) {

    conversationPanelWrapper.addAll(chatMessages);

    SwingUtilities.invokeLater(() -> {
      companionConversationScrollPan.validate();
      JScrollBar verticalBar = companionConversationScrollPan.getVerticalScrollBar();
      verticalBar.setValue(verticalBar.getMaximum());
    });

  }


}
