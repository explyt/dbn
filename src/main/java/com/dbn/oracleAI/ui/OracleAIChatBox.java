package com.dbn.oracleAI.ui;

import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class OracleAIChatBox extends JPanel {
  private static final Insets TEXT_AREA_INSETS = JBUI.insets(10);

  private static OracleAIChatBox instance;
  private JComboBox<String> profileComboBox;
  private JPanel chatBoxMainPanel;
  private JComboBox<String> companionConversationQuestion;
  private JTextPane companionConversationAnswersText;
  private JCheckBox explainSQLCheckbox, narrateCheckbox;
  private JPanel companionConversationPanel;
  private JScrollPane companionConversationPan;
  private JPanel companionCommandPanel;
  private JPanel MainCenter;


  private JComboBox AIModelComboBox;

  public DatabaseOracleAIManager currManager;

  private OracleAIChatBox(Project project) {
    currManager = project.getService(DatabaseOracleAIManager.class);
    initializeUI();
    this.setLayout(new BorderLayout(0, 0));
    this.add(chatBoxMainPanel);

  }
  public static OracleAIChatBox getInstance(Project project) {
    if (instance == null) {
      instance = new OracleAIChatBox(project);
    }
    return instance;
  }

  private void initializeUI() {
    initializeTextStyles();
    createTextFieldsPanel();
    configureTextArea(companionConversationAnswersText);

    ResourceBundle actions = ResourceBundle.getBundle("Messages", Locale.getDefault());

    explainSQLCheckbox.setText(actions.getString("explainSql.action"));

    profileComboBox.addActionListener(e -> {
      if(profileComboBox.getSelectedItem() == "New Profile...") currManager.openSettings();
    });

  }



  private void updateSliderLabels(JSlider slider, int currentValue) {
    Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
    labelTable.put(0, new JLabel("0"));
    labelTable.put(currentValue, new JLabel(String.valueOf((float) currentValue/10)));
    labelTable.put(10, new JLabel("1"));
    slider.setLabelTable(labelTable);
  }



  private void createTextFieldsPanel() {

    companionConversationQuestion.addItem("");
    companionConversationQuestion.addItem("What are the names of all the customers");
    companionConversationQuestion.addItem("Who joined after February 2022");
    companionConversationQuestion.addItem("Can you list all customers by their join date in ascending order");
    companionConversationQuestion.addItem("I need the email addresses and join dates of customers whose last name is Doe");
    //companionConversationQuestion.setPreferredSize(new Dimension(50, 50));
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

    Component editorComponent = companionConversationQuestion.getEditor().getEditorComponent();

    if (editorComponent instanceof JComponent) {
      JComponent editor = (JComponent) editorComponent;

      InputMap inputMap = editor.getInputMap(JComponent.WHEN_FOCUSED);
      ActionMap actionMap = editor.getActionMap();

      inputMap.put(KeyStroke.getKeyStroke("ENTER"), "submit");
      inputMap.put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");

      actionMap.put("submit", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          submitText();
        }
      });
    }


  }


  private void submitText() {
    ApplicationManager.getApplication().executeOnPooledThread(() ->
        processQuery(selectedAction()));
  }

  private ActionAIType selectedAction(){
    if(explainSQLCheckbox.isSelected()){
      return ActionAIType.EXPLAINSQL;
    } else {
      return ActionAIType.SHOWSQL;
    }
  }
  private void processQuery(ActionAIType actionType) {
    String output = currManager.queryOracleAI(Objects.requireNonNull(
      companionConversationQuestion.getSelectedItem()).toString(), actionType, Objects.requireNonNull(
      profileComboBox.getSelectedItem()).toString());
    ApplicationManager.getApplication().invokeLater(() -> setDisplayTextPane(output));
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
      e.printStackTrace();
    }
  }

  private void initializeTextStyles() {
    StyledDocument doc = companionConversationAnswersText.getStyledDocument();
    Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

    Style regular = doc.addStyle("regular", def);
    StyleConstants.setFontFamily(def, "SansSerif");

    Style s = doc.addStyle("code", regular);
    StyleConstants.setForeground(s, Color.getHSBColor(210, 50, 100));
  }

  public void updateProfiles(String selectdProfile) {


    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      List<Profile> fetchedProfiles = currManager.fetchProfiles();
      ApplicationManager.getApplication().invokeLater(() -> {
        profileComboBox.removeAllItems();
        populateComboBoxWithProfiles(fetchedProfiles);
        profileComboBox.setSelectedItem(selectdProfile);
      });
    });

  }

  private void populateComboBoxWithProfiles(List<Profile> profiles) {
    for (Profile profile : profiles) {
      profileComboBox.addItem(profile.getProfileName());
    }
    profileComboBox.addItem("New Profile...");
  }
  public OracleAIChatBoxState captureState(String connection) {
    OracleAIChatBoxState state = new OracleAIChatBoxState(connection);
    state.setSelectedOption((String) profileComboBox.getSelectedItem());
    state.setInputText(companionConversationQuestion.getSelectedItem().toString());
    state.setDisplayText(companionConversationAnswersText.getText());
    return state;
  }

  public void restoreState(OracleAIChatBoxState state) {
    if (state == null) return;
    profileComboBox.addItem(state.getSelectedOption());
    profileComboBox.setSelectedItem(state.getSelectedOption());
    this.updateProfiles(state.getSelectedOption());

    companionConversationQuestion.setSelectedItem(state.getInputText());
    companionConversationAnswersText.setText(state.getDisplayText());
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
