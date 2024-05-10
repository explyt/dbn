package com.dbn.oracleAI.config.ui;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.AIProfileItem;
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

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
  private JComboBox<AIProfileItem> profileComboBox;
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
  private JPanel profileMgntAttributesPanel;
  private JPanel attributesListPanel;
  private JPanel profileMgntTitlePanel;

  private JPanel windowActionPanel;
  private final AIProfileService profileSvc;
  private Project currProject;

  public ProfileManagementPanel(ConnectionHandler connection) {
    this.currProject = connection.getProject();
    this.profileSvc = currProject.getService(DatabaseOracleAIManager.class).getProfileService();
    // make sure we use box that stretch
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    this.add(mainPane);
    initComponent();
  }

  /**
   * initialize bindings
   */
  private void initComponent() {
    initializeButtons();
    updateProfileNames();
  }

  private void updateProfileNames() {
    profileSvc.getProfiles().thenAccept(pm -> {
      profileMap = pm.stream().collect(Collectors.toMap(Profile::getProfileName,
          Function.identity(),
          (existing, replacement) -> existing));

      if (!profileMap.isEmpty()) {
        currProfile = profileMap.values().iterator().next();
      } else {
        currProfile = null;
      }
      ApplicationManager.getApplication()
          .invokeLater(this::initializeUIComponents);
    }).exceptionally(e -> {
      ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(currProject, e.getCause().getMessage()));
      return null;
    });
  }

  /**
   * initialize UI components
   */
  private void initializeUIComponents() {
    if (currProfile != null) {
      populateProfileNames();
      updateWindow();
      initializeTable();
    } else {
      initializeEmptyWindow();
    }
  }

  /**
   * initialize action buttons
   */
  private void initializeButtons() {
    addProfileButton.setIcon(Icons.ACTION_ADD);
    addProfileButton.setToolTipText(messages.getString("ai.settings.profile.adding.tooltip"));
    ProfileComboBoxRenderer profileComboBoxRenderer = new ProfileComboBoxRenderer();
    profileComboBox.setRenderer(profileComboBoxRenderer);
    deleteProfileButton.addActionListener(event -> {
      Messages.showQuestionDialog(currProject, messages.getString(
              "ai.settings.profile.deletion.title"), messages.getString(
              "ai.settings.profile.deletion.message.prefix")
              + currProfile.getProfileName(),
          Messages.options(
              messages.getString("ai.messages.yes"),
              messages.getString("ai.messages.no")), 1,
          option -> when(option == 0,
              () -> {
                removeProfile(
                    currProfile);
              }));
    });
    editProfileButton.addActionListener(event -> {
      new ProfileEditionDialog(currProject, currProfile).display();
    });
    addProfileButton.addActionListener(event -> {
      ProfileEditionDialog profileEditionDialog = new ProfileEditionDialog(currProject, null);
      profileEditionDialog.display();
      profileEditionDialog.addWindowListener(new java.awt.event.WindowAdapter() {
        @Override
        public void windowClosed(java.awt.event.WindowEvent windowEvent) {
          updateProfileNames();
        }
      });
    });
  }

  /**
   * Renders the disabled profiles as such
   */
  private class ProfileComboBoxRenderer extends BasicComboBoxRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected,
          cellHasFocus);
      if (value instanceof AIProfileItem) {
        AIProfileItem item = (AIProfileItem) value;
        setEnabled(item.isEnabled());
      }
      return this;
    }
  }

  /**
   * Removes a profile from remote server
   *
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
      updateProfileNames();
    }).exceptionally(throwable -> {
      Messages.showErrorDialog(currProject,
          messages.getString("profiles.mgnt.attr.deletion.failed.title"),
          messages.getString("profiles.mgnt.attr.deletion.failed.msg"));

      return null;
    });
  }

  private void initializeEmptyWindow() {
    profileComboBox.setToolTipText("ai.messages.noitems");
    credentialField.setEnabled(false);
    providerField.setEnabled(false);
    modelField.setEnabled(false);
    deleteProfileButton.setEnabled(false);
    editProfileButton.setEnabled(false);
    makeDefaultProfileButton.setEnabled(false);
  }

  private void initializeFilledWindow() {
    credentialField.setEnabled(true);
    providerField.setEnabled(true);
    modelField.setEnabled(true);
    deleteProfileButton.setEnabled(true);
    editProfileButton.setEnabled(true);
    makeDefaultProfileButton.setEnabled(true);
  }

  /**
   * Placeholder to display a friendly value as opposed to blank
   * when value is null
   *
   * @param value
   * @return
   */
  private String fixAttributesPresentation(String value) {
    if (value != null) return value;
    return messages.getString("ai.messages.unknown");
  }

  /**
   * Updates the windows with current profile list
   */
  private void updateWindow() {
    if (currProfile != null) {
      initializeFilledWindow();
      populateTable(currProfile);
      credentialField.setText(fixAttributesPresentation(currProfile.getCredentialName()));
      providerField.setText(fixAttributesPresentation(currProfile.getProvider().toString()));
      modelField.setText(currProfile.getModel() == null ? currProfile.getProvider().getDefaultModel().name() : currProfile.getModel().name());
    } else {
      initializeEmptyWindow();
    }
  }

  /**
   * Populate the combo with profile list
   */
  private void populateProfileNames() {
    profileComboBox.removeActionListener(profileComboBoxAction);
    profileComboBox.removeAllItems();
    profileComboBox.addActionListener(profileComboBoxAction);
    if (profileMap != null) {
      profileMap.values().forEach(p -> profileComboBox.addItem(new AIProfileItem(p.getProfileName(), p.getProvider(), p.getModel(), p.isEnabled())));
    }
    if (currProfile != null) {
      profileComboBox.setSelectedItem(currProfile.getProfileName());
    }
  }

  private ActionListener profileComboBoxAction = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (profileComboBox.getSelectedItem() != null)
        currProfile = profileMap.get(profileComboBox.getSelectedItem().toString());
      updateWindow();
    }
  };

  private void initializeTable() {
    objListTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
      private final JTextField editor = new JTextField();

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null) {
          editor.setText(value.toString());
          editor.setFont(getFont().deriveFont(Font.PLAIN));
        } else {
          editor.setText("<all>");
          editor.setFont(getFont().deriveFont(Font.ITALIC));
        }

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
    String[] columnNames = {
        messages.getString("profile.mgmt.obj_table.header.name"),
        messages.getString("profile.mgmt.obj_table.header.owner")};
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
