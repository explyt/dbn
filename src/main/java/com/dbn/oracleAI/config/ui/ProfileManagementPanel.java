package com.dbn.oracleAI.config.ui;

import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.config.Profile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.dbn.common.util.Conditional.when;

/**
 * Profile management bindings
 */
public class ProfileManagementPanel extends JPanel {

  static private final ResourceBundle messages =
    ResourceBundle.getBundle("Messages", Locale.getDefault());

  private Map<String, Profile> profileMap;
  private Profile currProfile;
  private JPanel mainPane;
  private JTable objListTable;
  private JButton addProfileButton;
  private JComboBox<String> profileComboxBox;
  private JLabel credentialField;
  private JLabel modelField;
  private JLabel providerField;
  private JButton editProfileButton;
  private JButton deleteProfileButton;
  private JButton makeDefaultProfileButton;
  private JPanel profileMgntObjectListPanel;
  private JPanel profileAttrPanel;
  private JPanel profileSelectionPanel;
  private JPanel attributesActionPanel;
  private JPanel profileMgntTitlePanel;
  private JPanel profileMgntAttributesPanel;
  private JPanel attributesListPanel;

  private JPanel windowActionPanel;
  private final AIProfileService profileSvc;
  private Project currProject;

  public ProfileManagementPanel(ConnectionHandler connection) {
    this.currProject = connection.getProject();
    this.profileSvc = currProject.getService(DatabaseOracleAIManager.class).getProfileService();
    // make sure we use box that stretch
    this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

    this.add(mainPane);
    initComponent();
  }

  /**
   * initialize bindings
   */
  private void initComponent() {
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

  /**
   * initialize UI components
   */
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

  /**
   * populate profile map for current connection
   */
  private void initializeProfileNames() {
    profileComboxBox.removeAllItems();
    profileMap.forEach((name, profile) -> profileComboxBox.addItem(name));
    profileComboxBox.setSelectedItem(currProfile != null ? currProfile.getProfileName() : null);
    profileComboxBox.addActionListener(e -> {
      String selectedProfileName = (String) profileComboxBox.getSelectedItem();
      if(!Objects.equals(selectedProfileName, "<None>") && !Objects.equals(selectedProfileName, null)){
        currProfile = profileMap.get(selectedProfileName);
        updateWindow();
      }
    });
  }


  /**
   * initialize action buttons
   */
  private void initializeButtons() {
    deleteProfileButton.addActionListener(event -> {
      Messages.showQuestionDialog(currProject, messages.getString(
                                    "ai.settings.profile.deletion.title"), messages.getString(
                                    "ai.settings.profile.deletion.message.prefix")
                                                                           + currProfile.getProfileName(),
                                  Messages.options(
                                    messages.getString("ai.messages.yes"),
                                    messages.getString("ai.messages.no")), 1,
                                  option -> when(option == 0,
                                                 () -> removeProfile(
                                                   currProfile)));
    });
    editProfileButton.addActionListener(event -> {
      new ProfileEditionDialog(currProject, currProfile).display();
    });
    addProfileButton.addActionListener(event -> {
      new ProfileEditionDialog(currProject, currProfile).display();
    });
  }

  /**
   * Removes a profile from remote server
   * @param profile the profile ot be deleted
   */
  private void removeProfile(Profile profile) {
    profileSvc.deleteProfile(profile.getProfileName()).thenRun(() -> {
      profileMap.remove(profile.getProfileName());
      if (!profileMap.isEmpty()) {
        currProfile = profileMap.values().iterator().next();
      } else {
        currProfile = null;
      }
      updateWindow();
    }).exceptionally(throwable -> {
      //ApplicationManager.getApplication().invokeLater(() -> {
        Messages.showErrorDialog(currProject,
                                 messages.getString("profiles.mgnt.attr.deletion.failed.title"),
                                 messages.getString("profiles.mgnt.attr.deletion.failed.msg"));
      //});

      return null;
    });
  }
  private void initializeEmptyWindow(){
    profileComboxBox.setToolTipText("ai.messages.noitems");
    credentialField.setEnabled(false);
    providerField.setEnabled(false);
    modelField.setEnabled(false);
    deleteProfileButton.setEnabled(false);
    editProfileButton.setEnabled(false);
    makeDefaultProfileButton.setEnabled(false);
  }

  /**
   * Placeholder to display a friendly value as opposed to blank
   * when value is null
   * @param value
   * @return
   */
  private String fixAttributesPresentation(String value) {
    if (value != null ) return value;
    return messages.getString("ai.messages.unknown");
  }

  /**
   * Updates the windows with current profile list
   */
  private void updateWindow() {
    if (currProfile != null) {
      populateProfileNames();
      populateTable(currProfile);
      credentialField.setText(fixAttributesPresentation(currProfile.getCredentialName()));
      providerField.setText(fixAttributesPresentation(currProfile.getProvider().getAction()));
      modelField.setText(fixAttributesPresentation(currProfile.getModel()));
    } else {
      initializeEmptyWindow();
    }
  }

  /**
   * Populate the combo with profile list
   */
  private void populateProfileNames() {
    profileComboxBox.removeAllItems();
    if (profileMap != null) {
      profileMap.keySet().forEach(profileComboxBox::addItem);
    }
    if (currProfile != null) {
      profileComboxBox.setSelectedItem(currProfile.getProfileName());
    }
    profileComboxBox.addActionListener(e -> {
      String selectedProfileName = (String) profileComboxBox.getSelectedItem();
      currProfile = profileMap.get(selectedProfileName);
      updateWindow();
    });
  }

  private void initializeTable() {
    objListTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
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
    objListTable.setSelectionModel(new NullSelectionModel());
  }

  private void populateTable(Profile profile) {
    // TODO : use messages
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
    objListTable.setModel(tableModel);
  }


  private static class NullSelectionModel extends DefaultListSelectionModel {
    @Override
    public void setSelectionInterval(int index0, int index1) {
      super.setSelectionInterval(-1, -1);
    }
  }
}
