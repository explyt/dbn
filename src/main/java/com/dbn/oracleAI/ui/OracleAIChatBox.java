package com.dbn.oracleAI.ui;

import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
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
     * @param label the label 
     * @param effective is this effective or placeholder item ?
     */
    public AIProfileItem(String label,boolean effective) {
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

    public boolean isEffective(){
      return effective;
    }
    /**
     * Used to UI fw
     *
     * @return the label
     */
    @Override public String toString() {
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
      for (int i = 0; i < this.getSize()-1; i++) {
        if(this.getElementAt(i)!=ADD_PROFILE_COMBO_ITEM && this.getElementAt(i)!=NONE_COMBO_ITEM){
          result.add(this.getElementAt(i));
        }
      }
      return result;
    }

    /**
     * Get list of effective profiles.
     * remove non-effective profiles and the special 
     * @return
     */
    public List<AIProfileItem> getUsableProfiles() {
      return this.getAllProfiles().stream().filter(aiProfileItem -> aiProfileItem.effective == true).collect(
        Collectors.toList());
    }
  }

  private static final Insets TEXT_AREA_INSETS = JBUI.insets(10);

  private static OracleAIChatBox instance;
  private JComboBox<AIProfileItem> profileComboBox;
  private ProfileComboBoxModel profileListModel = new ProfileComboBoxModel();
  private JPanel chatBoxMainPanel;
  private JComboBox<String> companionConversationQuestion;
  private JTextPane companionConversationAnswersText;
  private JCheckBox explainSQLCheckbox;
  private JPanel companionConversationPanel;
  private JScrollPane companionConversationPan;
  private JPanel companionCommandPanel;
  private JPanel MainCenter;

  private JComboBox AIModelComboBox;
  private JPanel companionConversationPanelTop;
  private JProgressBar activityProgress;
  private JTextField chatBoxNotificationMessage;
  private JCheckBox checkBox1;

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
    this.add(chatBoxMainPanel,BorderLayout.CENTER);
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
    initializeTextStyles();
    createTextFieldsPanel();
    configureTextArea(companionConversationAnswersText);

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

  private class  ProfileComboBoxRenderer extends BasicComboBoxRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected,
                                                cellHasFocus);
      if (ADD_PROFILE_COMBO_ITEM.equals((AIProfileItem)value)) {
        setFont(getFont().deriveFont(Font.ITALIC));
      }
      return this;
    }
  }
  private void createTextFieldsPanel() {
    companionConversationQuestion.addItem(
      "What are the names of all the customers");
    companionConversationQuestion.addItem("Who joined after February 2022");
    companionConversationQuestion.addItem(
      "Can you list all customers by their join date in ascending order");
    companionConversationQuestion.addItem(
      "I need the email addresses and join dates of customers whose last name is Doe");
    setupEnterAction();
  }

  private void configureTextArea(JTextComponent textComponent) {
    textComponent.setEditable(false);
    textComponent.setMargin(TEXT_AREA_INSETS);
    if (textComponent instanceof JTextArea) {
      JTextArea textArea = (JTextArea) textComponent;
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
    }
  }

  private void setupEnterAction() {

    Component editorComponent =
      companionConversationQuestion.getEditor().getEditorComponent();

    if (editorComponent instanceof JComponent) {
      JComponent editor = (JComponent) editorComponent;

      InputMap inputMap = editor.getInputMap(JComponent.WHEN_FOCUSED);
      ActionMap actionMap = editor.getActionMap();

      inputMap.put(KeyStroke.getKeyStroke("ENTER"), "submit");
      inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");

      actionMap.put("submit", new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
          submitText();
        }
      });
    }

  }

  private void submitText() {
    startActivityNotifier("Submitting.. (we need a key in messages)");
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

    Objects.requireNonNull(companionConversationQuestion.getSelectedItem(),
                           "cannot be here without question been selected");

    AIProfileItem item = (AIProfileItem) profileComboBox.getSelectedItem();
    Objects.requireNonNull(item,
                           "cannot be here without profile been selected");
    if (ADD_PROFILE_COMBO_ITEM.equals(item)) {
      // TODO : we should never arrive here anyway
      return;
    }

    String question =
      companionConversationQuestion.getSelectedItem().toString();
    if (question.length() > 0) {
      String output =
        currManager.queryOracleAI(question, actionType, item.getLabel());
      ApplicationManager.getApplication()
                        .invokeLater(() -> {setDisplayTextPane(output);stopActivityNotifier();});
    }

  }

  public void setDisplayTextPane(String s) {
    try {
      StyledDocument doc = companionConversationAnswersText.getStyledDocument();
      doc.remove(0, doc.getLength());

      String[] tokens = s.split("```");
      boolean isCode = false;
      for (String token : tokens) {
        if (isCode) {
          doc.insertString(doc.getLength(), token, doc.getStyle("code"));
        } else {
          doc.insertString(doc.getLength(), token, doc.getStyle("regular"));
        }
        isCode = !isCode;
      }
    } catch (BadLocationException e) {
      // TODO : cannot be printing things like that
      e.printStackTrace();
    }
  }

  private void initializeTextStyles() {
    StyledDocument doc = companionConversationAnswersText.getStyledDocument();
    Style def = StyleContext.getDefaultStyleContext()
                            .getStyle(StyleContext.DEFAULT_STYLE);

    Style regular = doc.addStyle("regular", def);
    StyleConstants.setFontFamily(def, "SansSerif");

    Style s = doc.addStyle("code", regular);
    StyleConstants.setForeground(s, Color.getHSBColor(210, 50, 100));
  }

  /**
   * Updates profile combox box model by fetching
   * list of available profiles for the current connection
   */
  public CompletableFuture<Map<String, Profile>> updateProfiles() {
    return currManager.getProfileService().getProfiles();
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
    } catch (Exception e){
      System.out.println(e.getMessage());

    }
  }

  /**
   * Backuop current elements state fo rlater re-use
   * when we gonna enter here again with a new connection
   *
   * @return the state for this current companion
   */
  public OracleAIChatBoxState captureState(String currConnection) {
    AIProfileItem selectedProfile = (AIProfileItem) profileListModel.getSelectedItem();
    return OracleAIChatBoxState.builder()
        .currConnection(currConnection)
        .aiAnswers(companionConversationAnswersText.getText())
        .currentQuestionText(companionConversationQuestion.getSelectedItem().toString())
        .profiles(profileListModel.getAllProfiles())
        .selectedProfile(selectedProfile!=NONE_COMBO_ITEM?selectedProfile:null)
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
//        companionConversationQuestion.addItem(s);
//      }
      companionConversationQuestion.setSelectedItem(
          state.getCurrentQuestionText());
      companionConversationAnswersText.setText(state.getAiAnswers());

      if (profileListModel.getUsableProfiles().size() == 0) {
        companionConversationQuestion.setEnabled(false);
      } else {
        companionConversationQuestion.setEnabled(true);
      }

  }

  /**
   * Starts the spining wheel
   * TODO : try to be clever here...
   */
  private void startActivityNotifier(String message) {
     activityProgress.setIndeterminate(true);
     activityProgress.setVisible(true);
    chatBoxNotificationMessage.setVisible(true);
    chatBoxNotificationMessage.setText(message);
   }

  /**
   * Stops the spining wheel
   */private void stopActivityNotifier() {
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
   *
   */
  public void initState() {
    startActivityNotifier(messages.getString("companion.chat.fetching_profiles"));
    updateProfiles().thenAccept(finalFetchedProfiles -> {
      ApplicationManager.getApplication().invokeLater(() -> {
        profileListModel.removeAllElements();
        finalFetchedProfiles.forEach((pn, p) -> {
          profileListModel.addElement(new AIProfileItem(pn));
        });
        if(finalFetchedProfiles.size()==0){
          profileListModel.addElement(NONE_COMBO_ITEM);
        }
        profileListModel.addElement(ADD_PROFILE_COMBO_ITEM);

        if (profileListModel.getUsableProfiles().size() == 0) {
          companionConversationQuestion.setEnabled(false);
          companionConversationQuestion.setToolTipText(
            messages.getString("companion.chat.no_profile_yet.tooltip"));
          explainSQLCheckbox.setEnabled(false);
          explainSQLCheckbox.setToolTipText(
            messages.getString("companion.chat.no_profile_yet.tooltip"));
        } else {
          companionConversationQuestion.setEnabled(true);
          companionConversationQuestion.setToolTipText(
            messages.getString("companion.chat.question.enabled.tooltip"));
          explainSQLCheckbox.setEnabled(true);
          explainSQLCheckbox.setToolTipText(
            messages.getString("companion.explainsql.tooltip"));
        }
        stopActivityNotifier();
      });
    });
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
