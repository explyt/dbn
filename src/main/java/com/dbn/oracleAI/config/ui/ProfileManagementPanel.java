/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI.config.ui;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.ConnectionRef;
import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.AIProfileService;
import com.dbn.oracleAI.DatabaseOracleAIManager;
import com.dbn.oracleAI.ProfileEditionWizard;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionObjectListStep;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dbn.common.util.Conditional.when;
import static com.dbn.nls.NlsResources.txt;

/**
 * Profile management bindings
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 * @author Emmanuel Jannetti (emmanuel.jannetti@oracle.com)
 */
public class ProfileManagementPanel extends JPanel {

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
  private JPanel objListTableHeader;
  private JButton goToAssociatedObjects;

  private final ConnectionRef connection;
  private final DatabaseOracleAIManager manager;
  private final AIProfileService profileSvc;


  private JPanel windowActionPanel;

  public ProfileManagementPanel(ConnectionHandler connection) {
    this.connection = connection.ref();

    this.manager = DatabaseOracleAIManager.getInstance(connection.getProject());
    this.profileSvc = AIProfileService.getInstance(connection);
    // make sure we use box that stretch
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    objListTableHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));

    this.add(mainPane);

    initComponent();
  }

  /**
   * initialize bindings
   */
  private void initComponent() {
    initializeButtons();
    initializeUIComponents();
    initializeProfileNames();

    ApplicationManager.getApplication().invokeLater(() -> addProfileButton.requestFocusInWindow());

    goToAssociatedObjects.addActionListener(event -> {
      ProfileEditionWizard.showWizard(getConnection(), currProfile, profileMap, isCommit -> {
        if (isCommit) updateProfileNames();
      }, ProfileEditionObjectListStep.class);
    });

  }

  private void initializeProfileNames() {
//    List<Profile> profileList = profileSvc.getCachedProfiles();
    List<Profile> profileList = null;
    if (profileList != null) {
      profileMap = profileList.stream().collect(Collectors.toMap(Profile::getProfileName,
          Function.identity(),
          (existing, replacement) -> existing));

      if (!profileMap.isEmpty()) {
        currProfile = profileMap.values().iterator().next();
      } else {
        currProfile = null;
      }
      this.initializeUIComponents();
    } else {
      updateProfileNames();
    }
  }

  private void updateProfileNames() {
    profileSvc.list().thenAccept(pm -> {
      profileMap = pm.stream().collect(Collectors.toMap(Profile::getProfileName,
          Function.identity(),
          (existing, replacement) -> existing));

      if (!profileMap.isEmpty()) {
        currProfile = profileMap.values().iterator().next();
      } else {
        currProfile = null;
      }
      ApplicationManager.getApplication()
          .invokeLater(() -> {
//            manager.getProfileService().updateCachedProfiles(pm);
            this.initializeUIComponents();
          });
    }).exceptionally(e -> {
      ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(getProject(), e.getCause().getMessage()));
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
    editProfileButton.setIcon(Icons.ACTION_EDIT);
    deleteProfileButton.setIcon(Icons.ACTION_DELETE);
    makeDefaultProfileButton.setIcon(Icons.COMMON_CHECK);
    addProfileButton.setToolTipText(txt("ai.settings.profile.adding.tooltip"));
    ProfileComboBoxRenderer profileComboBoxRenderer = new ProfileComboBoxRenderer();
    profileComboBox.setRenderer(profileComboBoxRenderer);
    Project project = getProject();

    deleteProfileButton.addActionListener(event -> {
      Messages.showQuestionDialog(project, txt(
              "ai.settings.profile.deletion.title"), txt(
              "ai.settings.profile.deletion.message.prefix")
              + " " + currProfile.getProfileName(),
          Messages.options(
              txt("ai.messages.yes"),
              txt("ai.messages.no")), 1,
          option -> when(option == 0,
              () -> {
                removeProfile(
                    currProfile);
              }));
    });
    editProfileButton.addActionListener(event -> {
      ProfileEditionWizard.showWizard(getConnection(), currProfile, profileMap, isCommit -> {
        if (isCommit) updateProfileNames();
      }, null);
    });
    addProfileButton.addActionListener(event -> {
      ProfileEditionWizard.showWizard(getConnection(), null, profileMap, isCommit -> {
        if (isCommit) updateProfileNames();
      }, null);
    });

    makeDefaultProfileButton.addActionListener(event -> {
      AIProfileItem profile = new AIProfileItem(currProfile.getProfileName(), currProfile.getProvider(), currProfile.getModel(), currProfile.isEnabled());
      manager.updateDefaultProfile(getConnectionId(), profile);
    });
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }

  /**
   * Renders the disabled profiles as such
   */
  private class ProfileComboBoxRenderer extends BasicComboBoxRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (value instanceof AIProfileItem) {
        AIProfileItem item = (AIProfileItem) value;
        setEnabled(item.isEnabled());

        if (item.equals(manager.getDefaultProfile(getConnectionId()))) {
          setText(item.getName() + " (default)");
        } else {
          setText(item.getName());
        }
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
    profileSvc.delete(profile.getProfileName()).thenRun(() -> {
      profileMap.remove(profile.getProfileName());
      if (!profileMap.isEmpty()) {
        currProfile = profileMap.values().iterator().next();
      } else {
        currProfile = null;
      }
//      manager.getProfileService().removeCachedProfile(profile);
      updateProfileNames();
      updateWindow();
    }).exceptionally(throwable -> {
      Messages.showErrorDialog(getProject(),
          txt("profiles.mgnt.attr.deletion.failed.title"),
          txt("profiles.mgnt.attr.deletion.failed.msg"));

      return null;
    });
  }

  private void initializeEmptyWindow() {
    profileComboBox.setToolTipText(txt("ai.messages.noitems"));
    profileComboBox.setEnabled(false);
    credentialField.setEnabled(false);
    providerField.setEnabled(false);
    modelField.setEnabled(false);
    deleteProfileButton.setEnabled(false);
    editProfileButton.setEnabled(false);
    makeDefaultProfileButton.setEnabled(false);
    goToAssociatedObjects.setEnabled(false);
  }

  private void initializeFilledWindow() {
    profileComboBox.setToolTipText("");
    profileComboBox.setEnabled(true);
    credentialField.setEnabled(true);
    providerField.setEnabled(true);
    modelField.setEnabled(true);
    deleteProfileButton.setEnabled(true);
    editProfileButton.setEnabled(true);
    makeDefaultProfileButton.setEnabled(true);
    goToAssociatedObjects.setEnabled(true);
  }

  /**
   * Placeholder to display a friendly value as opposed to blank
   * when value is null
   *
   * @param value unfixed value to be displayed
   * @return a value suitable for displays
   */
  private String fixAttributesPresentation(String value) {
    if (value != null) return value;
    return txt("ai.messages.unknown");
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
      credentialField.setText(txt("ai.messages.unknown"));
      providerField.setText(txt("ai.messages.unknown"));
      modelField.setText(txt("ai.messages.unknown"));
      objListTable.setModel(new DefaultTableModel());
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
    ConnectionId connectionId = getConnectionId();
    if (manager.getDefaultProfile(connectionId) == null)
      manager.updateDefaultProfile(connectionId, (AIProfileItem) profileComboBox.getSelectedItem());
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

    objListTable.setSelectionModel(new NullSelectionModel());
    objListTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value != null) {
          setText(value.toString());
          setFont(getFont().deriveFont(Font.PLAIN));
        } else {
          setText("<all>");
          setFont(getFont().deriveFont(Font.ITALIC));
        }

        setBorder(null);
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        if (row % 2 == 0) {
          setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        } else {
          setBackground(isSelected ? table.getSelectionBackground() : new Color(236, 236, 236)); // light gray
        }

        return this;
      }
    });

  }

  private void populateTable(Profile profile) {
    String[] columnNames = {
        txt("profile.mgmt.obj_table.header.name"),
        txt("profile.mgmt.obj_table.header.owner")};
    Object[][] data = profile.getObjectList().stream()
        .map(obj -> new Object[]{obj.getName(), obj.getOwner()})
        .toArray(Object[][]::new);
    DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return (column == 1 && row == 0);
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


  private Project getProject() {
    return getConnection().getProject();
  }

  @NotNull
  private ConnectionHandler getConnection() {
    return connection.ensure();
  }

  private @NotNull ConnectionId getConnectionId() {
    return getConnection().getConnectionId();
  }
}
