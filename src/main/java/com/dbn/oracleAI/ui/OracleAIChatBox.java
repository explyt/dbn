package com.dbn.oracleAI.ui;

import com.dbn.common.util.Messages;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.types.AuthorType;
import com.esotericsoftware.kryo.kryo5.minlog.Log;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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

public class OracleAIChatBox extends JPanel {


  /**
   * Holder class for profile combox box
   * TODO : is that class create for each windows ?
   */
  static final class AIProfileItem {
    /**
     * Creates a new combo item
     *
     * @param label the label to be displayed in the combo
     */
    public AIProfileItem(String label) {
      this.label = label;
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
  private static final Insets CHAT_TEXT_AREA_INSETS = JBUI.insets(3, 5, 3, 100);
  private static final Insets PROMPT_TEXT_AREA_INSETS = JBUI.insets(15, 10, 15, 55);

  private static OracleAIChatBox instance;
  private JComboBox<AIProfileItem> profileComboBox;
  private ProfileComboBoxModel profileListModel = new ProfileComboBoxModel();
  private JPanel chatBoxMainPanel;
  //  private JTextPane companionConversationAnswersText;
  private JCheckBox explainSQLCheckbox;
  private JPanel companionConversationPanel;
  private JPanel conversationPanel;
  private JScrollPane companionConversationPan;
  private JPanel companionCommandPanel;
  private JPanel MainCenter;

  private JComboBox AIModelComboBox;
  private JPanel companionConversationPanelTop;
  private JProgressBar activityProgress;
  private JTextField chatBoxNotificationMessage;
  private JCheckBox checkBox1;
  private StyledDocument chatDocument;
  private JTextArea textArea;
  private final List<ChatMessage> chatMessages = new ArrayList<>();

  public static DatabaseOracleAIManager currManager;

  static private final ResourceBundle messages =
      ResourceBundle.getBundle("Messages", Locale.getDefault());

  /**
   * special profile combox item that lead to create a new profile
   */
  static final AIProfileItem ADD_PROFILE_COMBO_ITEM =
      new AIProfileItem(messages.getString("companion.profile.combobox.add"), false);

  static final AIProfileItem NONE_COMBO_ITEM =
      new AIProfileItem("<None>", false);

  private OracleAIChatBox() {
    this.setLayout(new BorderLayout(1, 1));
    this.add(chatBoxMainPanel, BorderLayout.CENTER);
    initializeUI();
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
    Log.info("FINE", "Header of chat window displayed");
    configureConversationPanel();
    Log.info("FINE", "Center of chat window displayed");
    configurePromptArea();
    Log.info("FINE", "Bottom of chat window displayed");
  }

  private void configureChatHeaderPanel() {
    explainSQLCheckbox.setText(messages.getString("companion.explainSql.action"));

    profileComboBox.setModel(profileListModel);
    profileComboBox.addActionListener(e -> {
      if (Objects.equals(profileComboBox.getSelectedItem(), ADD_PROFILE_COMBO_ITEM)) {
        profileComboBox.hidePopup();
        profileComboBox.setSelectedIndex(0);
        currManager.openSettings();
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
      if (ADD_PROFILE_COMBO_ITEM.equals((AIProfileItem) value)) {
        setFont(getFont().deriveFont(Font.ITALIC));
      }
      return this;
    }
  }


  private void configureConversationPanel() {
    conversationPanel = new JPanel();
    conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));

    companionConversationPan.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    companionConversationPan.add(conversationPanel);
    companionConversationPan.setViewportView(conversationPanel);
  }

  private void configurePromptArea() {

    JLayeredPane layeredPane = new JLayeredPane();
    layeredPane.setBounds(0, 0, 480, 50);
    layeredPane.setLayout(null);

    textArea = new JTextArea();
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setMargin(PROMPT_TEXT_AREA_INSETS);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBounds(0, 0, 480, 50);
    textArea.setBounds(0, 0, 430, 40);
    layeredPane.add(scrollPane, JLayeredPane.DEFAULT_LAYER);

    JButton button = new JButton(">");
    button.addActionListener(e -> {
      submitText();
    });
    button.setBounds(410, 3, 50, 35);
    layeredPane.add(button, JLayeredPane.PALETTE_LAYER);
    layeredPane.setPreferredSize(new Dimension(480, 40));
    JPanel aiQuestionPanel = new JPanel();
    aiQuestionPanel.setLayout(null);
    aiQuestionPanel.setPreferredSize(new Dimension(480, 40));
    aiQuestionPanel.add(layeredPane);
    companionConversationPanel.add(aiQuestionPanel, BorderLayout.SOUTH);
    int newX = 10;
    layeredPane.setLocation(newX, layeredPane.getY());
    InputMap inputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap actionMap = textArea.getActionMap();

    companionConversationPanel.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {
        updateLayout();
        updateTextPanesHeight();
      }

      private void updateLayout() {
        int newWidth = companionCommandPanel.getWidth();
        layeredPane.setBounds(0, 0, newWidth, layeredPane.getHeight());
        scrollPane.setBounds(0, 0, newWidth, scrollPane.getHeight());
        button.setBounds(scrollPane.getWidth() - 70, button.getY(), 50, 35);

        companionConversationPanel.revalidate();
        companionConversationPanel.repaint();
      }

      private void updateTextPanesHeight() {
        for (Component comp : conversationPanel.getComponents()) {
          if (comp instanceof JTextPane) {
            JTextPane pane = (JTextPane) comp;
            int newHeight = calculateTextPaneHeight(pane);
            pane.setPreferredSize(new Dimension(companionConversationPan.getWidth() * 3 / 4, newHeight));
            pane.setMaximumSize(new Dimension(companionConversationPan.getWidth() * 3 / 4, newHeight));
            if (pane.getBackground() == Color.WHITE) {
              pane.setAlignmentX(Component.LEFT_ALIGNMENT);
            } else {
              pane.setAlignmentX(Component.RIGHT_ALIGNMENT);
            }

          }
        }
        companionConversationPanel.revalidate();
        companionConversationPanel.repaint();
      }

    });

    inputMap.put(KeyStroke.getKeyStroke("ENTER"), "submit");
    inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");

    actionMap.put("submit", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        submitText();
      }
    });
    textArea.getDocument().addDocumentListener(new DocumentListener() {


      private void updateScrollPane() {
        ApplicationManager.getApplication().invokeLater(() -> {


          int totalHeight = textArea.getPreferredSize().height;
          int maxHeight = 100;
          int minHeight = 40;
          if (totalHeight <= maxHeight && totalHeight > minHeight) {
            resizePromptField(totalHeight);
          } else if (totalHeight > maxHeight) {
            resizePromptField(maxHeight);
          } else {
            resizePromptField(minHeight);
          }
          companionConversationPanel.revalidate();
          companionConversationPanel.repaint();
        });

      }

      private void resizePromptField(int height) {
        layeredPane.setBounds(0, 0, companionCommandPanel.getWidth(), height);
        scrollPane.setBounds(0, 0, companionCommandPanel.getWidth(), height);
        button.setBounds(scrollPane.getWidth() - 70, height - 45, 50, 35);
        layeredPane.setPreferredSize(new Dimension(companionCommandPanel.getWidth(), height));
        aiQuestionPanel.setPreferredSize(new Dimension(companionCommandPanel.getWidth(), height));
        scrollPane.setPreferredSize(new Dimension(companionCommandPanel.getWidth(), height));
        int parentWidth = companionConversationPanel.getWidth();
        int paneWidth = layeredPane.getPreferredSize().width;
        int newX = (parentWidth - paneWidth) / 2;
        layeredPane.setLocation(newX, layeredPane.getY());
        scrollPane.setVerticalScrollBarPolicy(height < 40 ? ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER : ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        updateScrollPane();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateScrollPane();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateScrollPane();
      }
    });
  }

  private void submitText() {
    startActivityNotifier("Submitting..");
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

    Objects.requireNonNull(textArea.getText(),
        "cannot be here without question been selected");

    AIProfileItem item = (AIProfileItem) profileComboBox.getSelectedItem();

    String question = textArea.getText();
    if (question.length() > 0) {
      textArea.setText("");
      ChatMessage inputChatMessage = new ChatMessage(question, AuthorType.USER);
      chatMessages.add(inputChatMessage);
      appendMessageToChat(inputChatMessage);
      try {
        String output =
            currManager.queryOracleAI(question, actionType, item.getLabel());
        ChatMessage outPutChatMessage = new ChatMessage(output, AuthorType.AI);
        chatMessages.add(outPutChatMessage);
        appendMessageToChat(outPutChatMessage);
        ApplicationManager.getApplication()

            .invokeLater(() -> {
            });
      } catch (QueryExecutionException | SQLException e) {

        ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currManager.getProject(), e.getMessage()));

      } finally {
        stopActivityNotifier();
      }
    }


  }

  /**
   * Updates profile combox box model by fetching
   * list of available profiles for the current connection
   */
  public CompletableFuture<Map<String, Profile>> updateProfiles() {
    return currManager.getProfileService().getProfiles().thenApply(pl -> pl.stream()
            .collect(Collectors.toMap(Profile::getProfileName,
                Function.identity(),
                (existing, replacement) -> existing)))
        .exceptionally(e -> {
          ApplicationManager.getApplication().invokeLater(() ->
              Messages.showErrorDialog(currManager.getProject(), "Cannot get profile list: " + e.getCause().getMessage()));
          return null;
        });

  }

  /**
   * Updates profile combox box model with new profile item list
   */
  public void updateProfiles(List<AIProfileItem> items) {
    try {
      profileListModel.removeAllElements();
      profileListModel.addAll(items);
      if (items.isEmpty()) {
        profileListModel.addElement(NONE_COMBO_ITEM);
        profileComboBox.setSelectedItem(NONE_COMBO_ITEM);
      }
      profileListModel.addElement(ADD_PROFILE_COMBO_ITEM);
      profileListModel.setSelectedItem(profileComboBox.getItemAt(0));
    } catch (Exception e) {
      // TODO fix this
      System.out.println(e.getMessage());

    }
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
        .aiAnswers(chatMessages)
//        .currentQuestionText(companionConversationQuestion.getSelectedItem().toString())
        .profiles(profileListModel.getAllProfiles())
        .selectedProfile((selectedProfile != null && selectedProfile.isEffective()) ? selectedProfile : null)
        .build();
  }

  /**
   * Restores the element state according to the current connection.
   *
   * @param state the state that should be applied
   */
  public void restoreState(OracleAIChatBoxState state) {
    assert state != null : "cannot be null";
    this.updateProfiles(state.getProfiles());
//      for (String s : state.getQuestionHistory()) {
//        companionConversationQuestion.addItems(s);
//      }

//      companionConversationQuestion.setSelectedItem(
//          state.getCurrentQuestionText());
    chatMessages.clear();
    chatMessages.addAll(state.getAiAnswers());
    populateChatPanel();

//      if (profileListModel.getUsableProfiles().size() == 0) {
//        companionConversationQuestion.setEnabled(false);
//      } else {
//        companionConversationQuestion.setEnabled(true);
//      }

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
    startActivityNotifier(messages.getString("companion.chat.fetching_profiles"));
    updateProfiles().thenAccept(finalFetchedProfiles -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        profileListModel.removeAllElements();
        finalFetchedProfiles.forEach((pn, p) -> {
          profileListModel.addElement(new AIProfileItem(pn));
        });
        if (finalFetchedProfiles.size() == 0) {
          profileListModel.addElement(NONE_COMBO_ITEM);
        }
        profileListModel.addElement(ADD_PROFILE_COMBO_ITEM);

        if (profileListModel.getUsableProfiles().size() == 0) {

//          companionConversationQuestion.setEnabled(false);
//          companionConversationQuestion.setToolTipText(
//            messages.getString("companion.chat.no_profile_yet.tooltip"));
          explainSQLCheckbox.setEnabled(false);
          explainSQLCheckbox.setToolTipText(
              messages.getString("companion.chat.no_profile_yet.tooltip"));
        } else {
//          companionConversationQuestion.setEnabled(true);
//          companionConversationQuestion.setToolTipText(
//            messages.getString("companion.chat.question.enabled.tooltip"));
          explainSQLCheckbox.setEnabled(true);
          explainSQLCheckbox.setToolTipText(
              messages.getString("companion.explainsql.tooltip"));
        }
        stopActivityNotifier();
      });
    }).exceptionally(e -> {
      ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currManager.getProject(), e.getCause().getMessage()));
      stopActivityNotifier();
      return null;
    });
  }

  private void populateChatPanel() {
    for (ChatMessage message : chatMessages) {
      appendMessageToChat(message);
    }
  }

  private void appendMessageToChat(ChatMessage chatMessage) {
    JTextPane messagePane = createMessagePane(chatMessage);

    conversationPanel.add(messagePane);
    conversationPanel.revalidate();
    conversationPanel.repaint();
  }

  private JTextPane createMessagePane(ChatMessage chatMessage) {
    JTextPane messagePane = new JTextPane();
    messagePane.setText(chatMessage.getMessage());
    messagePane.setEditable(false);

    // Set background based on the author
    if (chatMessage.getAuthor() == AuthorType.USER) {
//      messagePane.setBorder(
//          BorderFactory.createEmptyBorder(3, 30, 3, 5));
      messagePane.setBackground(Color.LIGHT_GRAY);
//      alignText(messagePane, StyleConstants.ALIGN_RIGHT);
      messagePane.setAlignmentX(Component.LEFT_ALIGNMENT);

    } else {
      messagePane.setBackground(Color.WHITE);
//      messagePane.setMargin(CHAT_TEXT_AREA_INSETS);
//      messagePane.setBorder(
//          BorderFactory.createEmptyBorder(3, 5, 3, 100));

//      alignText(messagePane, StyleConstants.ALIGN_LEFT);
      messagePane.setAlignmentX(Component.RIGHT_ALIGNMENT);

    }
//    messagePane.setMargin(CHAT_TEXT_AREA_INSETS);


    int height = calculateTextPaneHeight(messagePane);
    messagePane.setPreferredSize(new Dimension(messagePane.getPreferredSize().width * 3 / 4, height));
    messagePane.setMaximumSize(new Dimension(messagePane.getPreferredSize().width * 3 / 4, height));

    return messagePane;
  }

  /**
   * Aligns text inside a JTextPane.
   *
   * @param pane      the JTextPane to align text within.
   * @param alignment the alignment value (e.g., StyleConstants.ALIGN_LEFT, StyleConstants.ALIGN_RIGHT).
   */
  private void alignText(JTextPane pane, int alignment) {
    StyledDocument doc = pane.getStyledDocument();
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    StyleConstants.setAlignment(attrs, alignment);
    doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
  }

  private int calculateTextPaneHeight(JTextPane textPane) {
    textPane.setSize(conversationPanel.getWidth(), Integer.MAX_VALUE);
    View view = textPane.getUI().getRootView(textPane).getView(0);
    view.setSize(conversationPanel.getWidth(), Integer.MAX_VALUE);
    int preferredHeight = (int) view.getPreferredSpan(View.Y_AXIS);
    return preferredHeight;
  }


}
