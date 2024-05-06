package com.dbn.oracleAI.ui;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.types.AuthorType;
import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.types.ProviderType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import net.miginfocom.swing.MigLayout;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.SQLException;
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

public class OracleAIChatBox extends JPanel {


  private boolean shouldPromptTextAreaListen = true;

  /**
   * Holder class for profile combox box
   */
  static final class AIProfileItem {
    /**
     * Creates a new combo item
     *
     * @param label the label to be displayed in the combo
     */
    public AIProfileItem(String label, ProviderType provider, ProviderModel model) {
      this.label = label;
      this.provider = provider;
      this.model = model;
      this.effective = true;
    }

    /**
     * Creates a new combo item
     *
     * @param label     the label
     * @param effective is this effective or placeholder item ?
     */
    public AIProfileItem(String label, boolean effective) {
      this.label = label;
      this.effective = effective;
    }

    /**
     * Gets the label of this combo item
     *
     * @return the label
     */
    public String getLabel() {
      return label;
    }

    public boolean isEffective() {
      return effective;
    }

    /**
     * Used to UI fw
     *
     * @return the label
     */
    @Override
    public String toString() {
      return label;
    }

    /**
     * the label of this combo item
     */
    private String label;
    private ProviderType provider;
    private ProviderModel model;

    /**
     * Checks that this is effective/usable profile
     * basic example is that the 'New profile...' is not
     */
    private boolean effective;

  }

  /**
   * Dedicated class for NL2SQL profile model.
   */
  class ProfileComboBoxModel extends DefaultComboBoxModel<AIProfileItem> {

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
      return this.getAllProfiles().stream().filter(aiProfileItem -> aiProfileItem.effective).collect(
          Collectors.toList());
    }
  }

  private static final Logger LOG = Logger.getInstance(OracleAIChatBox.class);

  private static OracleAIChatBox instance;
  private JComboBox<AIProfileItem> profileComboBox;
  private ProfileComboBoxModel profileListModel = new ProfileComboBoxModel();
  private JPanel chatBoxMainPanel;
  private JCheckBox explainSQLCheckbox;
  private JPanel companionConversationPanel;
  private JPanel conversationPanel;
  private JScrollPane companionConversationScrollPan;
  private JPanel companionCommandPanel;
  private JPanel MainCenter;

  private JComboBox<ProviderModel> aiModelComboBox;
  private JPanel companionConversationPanelTop;
  private JProgressBar activityProgress;
  private JTextField chatBoxNotificationMessage;
  private JPanel companionConversationPanelBottom;
  private JButton promptButton;
  private JTextArea promptTextArea;
  private JPanel buttonPanel;
  private final List<ChatMessage> chatMessages = new ArrayList<>();

  public static DatabaseOracleAIManager currManager;

  private boolean canSubmitText = false;

  static private final ResourceBundle messages =
      ResourceBundle.getBundle("Messages", Locale.getDefault());

  /**
   * special profile combobox item that lead to create a new profile
   */
  static final AIProfileItem ADD_PROFILE_COMBO_ITEM =
      new AIProfileItem(messages.getString("companion.profile.combobox.add"), false);

  /**
   * special profile combobox item that's a placeholder for when there is no effective profiles
   */

  private OracleAIChatBox() {
    initializeUI();
    this.setLayout(new BorderLayout(1, 1));
    this.add(chatBoxMainPanel, BorderLayout.CENTER);
  }

  public static OracleAIChatBox getInstance(Project project) {
    currManager = project.getService(DatabaseOracleAIManager.class);
    if (instance == null) {
      instance = new OracleAIChatBox();
    }
    return instance;
  }

  private void initializeUI() {
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

    profileComboBox.addActionListener(e -> {
      AIProfileItem currProfileItem = (AIProfileItem) profileComboBox.getSelectedItem();
      if (!Objects.equals(currProfileItem, ADD_PROFILE_COMBO_ITEM)) {
        updateModelsComboBox(currProfileItem);
      } else {
        if (e.getModifiers() == BUTTON1_MASK) {
          profileComboBox.hidePopup();
          profileComboBox.setSelectedIndex(0);
          currManager.openSettings();
        }
      }
    });
    profileComboBox.setRenderer(new ProfileComboBoxRenderer());
  }

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
      return this;
    }
  }

  /**
   * Initializes the panel to display messages
   */
  private void configureConversationPanel() {
    conversationPanel.setLayout(new MigLayout("fillx"));
  }

  /**
   * Initializes the prompt panel, with a text area and a button
   * Adding event listeners
   */
  private void configurePromptArea() {
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
        if (canSubmitText) {
          submitText();
        }
      }
    });
    promptTextArea.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        LOG.debug("focusGained");
        // no need for indirection use promptTextArea directly
        String currentText = promptTextArea.getText();
        if (currentText != null &&
                currentText.compareTo(messages.getString("companion.chat.prompt.tooltip")) == 0 &&
                promptTextArea.getFont().getStyle() == Font.ITALIC) {
          // remove hint for user
          setPromptAreaIdle(false);
        }
      }

      @Override
      public void focusLost(FocusEvent e) {
        LOG.debug("focusLost");
        // no need for indirection
        if (promptTextArea.getText().isEmpty()) {
          // provide hint to user
          setPromptAreaIdle(true);
        }
      }
    });

    promptTextArea.getDocument().addDocumentListener(new DocumentListener() {

      private void treatIsOnlyBlanks(javax.swing.text.Document doc) {
        try {
          if (doc.getText(0, doc.getLength()).isBlank()) {
            promptButton.setEnabled(false);
            canSubmitText = false;
          } else {
            promptButton.setEnabled(true);
            canSubmitText = true;
          }
        } catch (BadLocationException e) {
          // won't happen anyway
          throw new RuntimeException(e);
        }
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        LOG.debug("insertUpdate");
        if (shouldPromptTextAreaListen)
          treatIsOnlyBlanks(e.getDocument());
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        LOG.debug("removeUpdate");
        if (shouldPromptTextAreaListen)
          treatIsOnlyBlanks(e.getDocument());
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        LOG.debug("changedUpdate");
        if (shouldPromptTextAreaListen)
          treatIsOnlyBlanks(e.getDocument());
      }
    });

    setPromptAreaIdle(true);

  }


  private void setPromptAreaIdle(boolean isIdle) {
  LOG.debug("setPromptAreaIdle : "+ isIdle);
   try {
     shouldPromptTextAreaListen = false;
     if (isIdle) {
       promptTextArea.setText(messages.getString("companion.chat.prompt.tooltip"));
       promptTextArea.setFont(promptTextArea.getFont().deriveFont(Font.ITALIC));
     } else {
       promptTextArea.setText("");
       promptTextArea.setFont(promptTextArea.getFont().deriveFont(Font.PLAIN));
     }
   } finally {
     shouldPromptTextAreaListen = true;
   }
}
  private void submitText() {
    startActivityNotifier(messages.getString("companion.chat.submitting"));
    ApplicationManager.getApplication()
        .executeOnPooledThread(
            () -> processQuery(selectedAction()));
  }

  private ActionAIType selectedAction() {
    if (explainSQLCheckbox.isSelected()) {
      return ActionAIType.EXPLAINSQL;
    } else {
      return ActionAIType.SHOWSQL;
    }
  }

  private void processQuery(ActionAIType actionType) {
    LOG.debug("Starting processQuery with actionType: " + actionType);
    Objects.requireNonNull(promptTextArea.getText(),
            "cannot be here without question been selected");

    AIProfileItem item = (AIProfileItem) profileComboBox.getSelectedItem();

    // we are protected as we cannot arrive if that's only blanks
    String question = promptTextArea.getText().trim();
    // TODO : do we want to append the message even in case of error
    ChatMessage inputChatMessage = new ChatMessage(question, AuthorType.USER);
    chatMessages.add(inputChatMessage);
    appendMessageToChat(inputChatMessage);
    try {
      String output =
              currManager.queryOracleAI(question, actionType, item.getLabel(),
                      ((ProviderModel) aiModelComboBox.getSelectedItem()).getApiName());
      ChatMessage outPutChatMessage = new ChatMessage(output, AuthorType.AI);
      chatMessages.add(outPutChatMessage);
      appendMessageToChat(outPutChatMessage);
      LOG.debug("Query processed successfully.");
    } catch (QueryExecutionException | SQLException e) {
      LOG.error("Error processing query", e);
      ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currManager.getProject(), e.getMessage()));
    } finally {
      setPromptAreaIdle(true);
      stopActivityNotifier();
    }
  }

  /**
   * Updates profile combobox box model by fetching
   * list of available profiles for the current connection
   */
  public CompletableFuture<Map<String, Profile>> updateProfiles() {
    return currManager.getProfileService().getProfiles().thenApply(pl -> pl.stream()
            .collect(Collectors.toMap(Profile::getProfileName,
                Function.identity(),
                (existing, replacement) -> existing)))
        .exceptionally(e -> {
          ApplicationManager.getApplication().invokeLater(() ->
              Messages.showErrorDialog(currManager.getProject(), messages.getString("companion.chat.profiles.failure") + e.getCause().getMessage()));
          return null;
        });

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
        .aiAnswers(new ArrayList<>(chatMessages))
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
    chatMessages.clear();
    chatMessages.addAll(state.getAiAnswers());
    populateChatPanel();
    try {
      shouldPromptTextAreaListen = false;
      promptTextArea.setText(state.getCurrentQuestionText());
    } finally {
      shouldPromptTextAreaListen = true;
    }
  }

  /**
   * Starts the spinning wheel
   * TODO : try to be clever here...
   */
  private void startActivityNotifier(String message) {
    activityProgress.setIndeterminate(true);
    activityProgress.setVisible(true);
    chatBoxNotificationMessage.setVisible(true);
    chatBoxNotificationMessage.setText(message);
  }

  /**
   * Stops the spinning wheel
   */
  private void stopActivityNotifier() {
    activityProgress.setMinimum(0);
    activityProgress.setMaximum(0);
    activityProgress.setIndeterminate(false);
    activityProgress.setVisible(false);

    chatBoxNotificationMessage.setVisible(false);
    chatBoxNotificationMessage.setText("");

  }

  /**
   * Restores the element state from scratch.
   * basically called only once ny switchConnection()
   */
  public void initState() {
    LOG.debug("Initialize new state");
    startActivityNotifier(messages.getString("companion.chat.fetching_profiles"));
    updateProfiles().thenAccept(finalFetchedProfiles -> {
      LOG.debug("Profiles fetched successfully");
      ApplicationManager.getApplication().invokeLater(() -> {
        profileListModel.removeAllElements();
        finalFetchedProfiles.forEach((pn, p) -> {
          profileListModel.addElement(new AIProfileItem(pn, p.getProvider(), p.getModel()));
        });

        profileListModel.addElement(ADD_PROFILE_COMBO_ITEM);

        if (profileListModel.getUsableProfiles().isEmpty()) {
          explainSQLCheckbox.setEnabled(false);
          promptTextArea.setEnabled(false);
          promptButton.setEnabled(false);
          explainSQLCheckbox.setToolTipText(
              messages.getString("companion.chat.no_profile_yet.tooltip"));
          promptTextArea.setToolTipText(
              messages.getString("companion.chat.no_profile_yet.tooltip"));
          promptButton.setToolTipText(
              messages.getString("companion.chat.no_profile_yet.tooltip"));
        } else {
          explainSQLCheckbox.setEnabled(true);
          promptTextArea.setEnabled(true);
          explainSQLCheckbox.setToolTipText(
              messages.getString("companion.explainsql.tooltip"));
          promptTextArea.setToolTipText(messages.getString("companion.chat.prompt.tooltip"));
          promptButton.setToolTipText(messages.getString("companion.chat.prompt.button.tooltip"));
        }
        AIProfileItem currProfileItem = (AIProfileItem) profileComboBox.getSelectedItem();
        if (currProfileItem != null && currProfileItem.provider != null) updateModelsComboBox(currProfileItem);
        stopActivityNotifier();
      });
    }).exceptionally(e -> {
      LOG.error("Failed to fetch profiles", e);
      ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currManager.getProject(), e.getCause().getMessage()));
      stopActivityNotifier();
      return null;
    });
  }

  private void updateModelsComboBox(AIProfileItem currPofileItem) {
    aiModelComboBox.removeAllItems();
    for (ProviderModel model : currPofileItem.provider.getModels()) {
      aiModelComboBox.addItem(model);
    }
    if (currPofileItem.model != null) {
      aiModelComboBox.setSelectedItem(currPofileItem.model);
    } else {
      // select the default
      aiModelComboBox.setSelectedItem(currPofileItem.provider.getDefaultModel());
    }
  }

  private void populateChatPanel() {
    conversationPanel.removeAll();
    for (ChatMessage message : chatMessages) {
      appendMessageToChat(message);
    }
  }

  private void appendMessageToChat(ChatMessage chatMessage) {
    JPanel messagePane = createMessagePane(chatMessage);

    conversationPanel.add(messagePane, chatMessage.getAuthor() == AuthorType.AI ? "wrap, w ::80%" : "wrap, al right, w ::80%");
    SwingUtilities.invokeLater(() -> {
      companionConversationScrollPan.revalidate();
      companionConversationScrollPan.repaint();
      //companionConversationPanel.revalidate();
      //companionConversationPanel.repaint();
      JScrollBar verticalBar = companionConversationScrollPan.getVerticalScrollBar();
      verticalBar.setValue(verticalBar.getMaximum());
    });

  }


  private JPanel createMessagePane(ChatMessage chatMessage) {
    JIMSendTextPane messagePane = new JIMSendTextPane();
    messagePane.setText(chatMessage.getMessage());
    messagePane.setAuthorColor(chatMessage.getAuthor());
    return messagePane;
  }

}
