package com.dbn.oracleAI.config.ui;

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Profile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProfileManagementPanel extends JPanel {

  static private final ResourceBundle messages =
    ResourceBundle.getBundle("Messages", Locale.getDefault());

  private Map<String, Profile> profileMap;
  private Profile currProfile;
  private JPanel mainPane;
  private JTable table1;
  private JButton button1;
  private JComboBox<String> comboBox1;
  private JLabel credentialField;
  private JLabel modelField;
  private JLabel providerField;
  private JButton editButton;
  private JButton deleteButton;
  private JButton makeDefaultButton;
  private final AIProfileService profileSvc;
  private Project currProject;

  public ProfileManagementPanel(ConnectionHandler connection) {
    this.profileSvc = currProject.getService(DatabaseOracleAIManager.class).getProfileService();
    this.currProject = connection.getProject();
    initComponent();
  }

  private void initComponent() {
    this.add(mainPane);
      profileSvc.getProfiles().thenAccept(pm -> {
        profileMap = pm;
        if (!profileMap.isEmpty()) {
          currProfile = profileMap.values().iterator().next();
        } else {
          currProfile = null;
        }
        ApplicationManager.getApplication()
                          .invokeLater(this::initializeUIComponents);
      });


  }

  private void initializeUIComponents() {
    if (currProfile != null) {
      initializeProfileNames();
      initializeButtons();
      updateWindow();
      initializeTable();
    } else {
      initializeEmptyWindow();
    }
  }

  private void initializeProfileNames() {
    comboBox1.removeAllItems();
    profileMap.forEach((name, profile) -> comboBox1.addItem(name));
    comboBox1.setSelectedItem(currProfile != null ? currProfile.getProfileName() : null);
    comboBox1.addActionListener(e -> {
      String selectedProfileName = (String) comboBox1.getSelectedItem();
      if(!Objects.equals(selectedProfileName, "<None>") && !Objects.equals(selectedProfileName, null)){
        currProfile = profileMap.get(selectedProfileName);
        updateWindow();
      }
    });
  }

  private void initializeButtons() {
    deleteButton.addActionListener(e -> {
      Messages.showQuestionDialog(currProject,
                                  messages.getString("ai.settings.profile.deletion.title"),
                                  messages.getString("ai.settings.profile.deletion.message.prefix") + currProfile.getProfileName(),
                                  Messages.options(
                                    messages.getString("ai.messages.yes"),
                                    messages.getString("ai.messages.no")), 1,
                                  option -> {
                                    if (option == 0) {
                                      removeProfile(currProfile);
                                      updateWindow();
                                      }
                                    });
                                  });
  }

  private void removeProfile(Profile profile) {
    profileSvc.deleteProfile(profile.getProfileName()).thenRun(() -> {
      profileMap.remove(profile.getProfileName());
      if (!profileMap.isEmpty()) {
        currProfile = profileMap.values().iterator().next();
      } else {
        currProfile = null;
      }
    });
  }
  private void initializeEmptyWindow(){
    comboBox1.addItem("<None>");
    credentialField.setText("None");
    providerField.setText("None");
    modelField.setText("None");
    deleteButton.disable();
    editButton.disable();
    makeDefaultButton.disable();
  }
  private void updateWindow() {
    if (currProfile != null) {
      populateProfileNames();
      populateTable(currProfile);
      credentialField.setText(currProfile.getCredentialName());
      providerField.setText(currProfile.getProvider().getAction());
      modelField.setText(currProfile.getModel());
    } else {
      initializeEmptyWindow();
    }
  }

  private void populateProfileNames() {
    comboBox1.removeAllItems();
    if (profileMap != null) {
      profileMap.keySet().forEach(comboBox1::addItem);
    }
    if (currProfile != null) {
      comboBox1.setSelectedItem(currProfile.getProfileName());
    }
    comboBox1.addActionListener(e -> {
      String selectedProfileName = (String) comboBox1.getSelectedItem();
      currProfile = profileMap.get(selectedProfileName);
      updateWindow();
    });
  }

  private void initializeTable() {
    table1.setDefaultRenderer(Object.class, new TableCellRenderer() {
      private final JTextField editor = new JTextField();

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        editor.setText((value != null) ? value.toString() : "");
        editor.setBorder(null);
        editor.setEditable(false);
        editor.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        editor.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        return editor;
      }
    });
    table1.setSelectionModel(new NullSelectionModel());
  }

  private void populateTable(Profile profile) {
    String[] columnNames = {"Table/View Name", "Owner"};
    Object[][] data = profile.getObjectList().stream()
        .map(obj -> new Object[]{obj.getName(), obj.getOwner()})
        .toArray(Object[][]::new);
    DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
      @Override
      public boolean isCellEditable(int row, int column) {
        // Prevent cell editing
        return false;
      }
    };
    table1.setModel(tableModel);
  }

  private static class NullSelectionModel extends DefaultListSelectionModel {
    @Override
    public void setSelectionInterval(int index0, int index1) {
      super.setSelectionInterval(-1, -1);
    }
  }
}
