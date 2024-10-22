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

package com.dbn.connection.ui;

import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.form.field.DBNFormFieldAdapter;
import com.dbn.common.ui.form.field.JComponentCategory;
import com.dbn.common.ui.util.TextFields;
import com.dbn.common.util.Commons;
import com.dbn.connection.AuthenticationType;
import com.dbn.connection.config.ConnectionDatabaseSettings;
import com.dbn.connection.config.ui.ConnectionDatabaseSettingsForm;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dbn.common.ui.form.field.JComponentFilter.accessibleClassifiedAs;
import static com.dbn.common.ui.form.field.JComponentFilter.array;
import static com.dbn.common.ui.form.field.JComponentFilter.classifiedAs;
import static com.dbn.common.ui.form.field.JComponentFilter.inaccessible;
import static com.dbn.common.ui.util.ComboBoxes.getSelection;
import static com.dbn.common.ui.util.ComboBoxes.initComboBox;
import static com.dbn.common.ui.util.ComboBoxes.setSelection;
import static com.dbn.common.ui.util.TextFields.onTextChange;
import static com.dbn.connection.AuthenticationType.USER;
import static com.dbn.connection.AuthenticationType.USER_PASSWORD;
import static com.dbn.connection.ui.ConnectionAuthenticationFieldsForm.FieldCategory.CACHEABLE_FIELDS;

public class ConnectionAuthenticationFieldsForm extends DBNFormBase {
    enum FieldCategory implements JComponentCategory {
        CACHEABLE_FIELDS,
    }

    private JComboBox<AuthenticationType> authTypeComboBox;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JPanel mainPanel;
    private JLabel userLabel;
    private JLabel passwordLabel;
    private JLabel tokenConfigFileLabel;
    private JLabel tokenProfileLabel;
    private JCheckBox tokenBrowserAuthCheckBox;
    private TextFieldWithBrowseButton tokenConfigFileTextField;
    private JComboBox<String> tokenProfileComboBox;

    ConnectionAuthenticationFieldsForm(@NotNull ConnectionDatabaseSettingsForm parentComponent) {
        super(parentComponent);
        
        tokenConfigFileTextField.addBrowseFolderListener(
                "Select OCI Configuration File",
                "Folder must contain an oci config file (usually ~/.oci/config)",
                null, new FileChooserDescriptor(true, false, false, false, false, false));
        onTextChange(tokenConfigFileTextField, e -> refreshTokenProfileOptions());
        
        initComboBox(authTypeComboBox, AuthenticationType.values());

        ActionListener actionListener = e -> updateAuthenticationFields();
        authTypeComboBox.addActionListener(actionListener);
        tokenBrowserAuthCheckBox.addActionListener(actionListener);
        tokenBrowserAuthCheckBox.setVisible(false);

        initFields();
    }

    private void initFields() {
        DBNFormFieldAdapter fieldAdapter = getFieldAdapter();

        // init visibility and accessibility conditions
        fieldAdapter.initFieldsVisibility(()    -> isUserAuth(),          array(userLabel, userTextField));
        fieldAdapter.initFieldsVisibility(()    -> isPasswordAuth(),      array(passwordLabel, passwordField));
        fieldAdapter.initFieldsVisibility(()    -> isTokenAuth(),         array(tokenBrowserAuthCheckBox, tokenConfigFileLabel, tokenConfigFileTextField, tokenProfileLabel, tokenProfileComboBox));
        fieldAdapter.initFieldsAccessibility(() -> !isBrowserTokenAuth(), array(tokenConfigFileLabel, tokenConfigFileTextField, tokenProfileLabel, tokenProfileComboBox));

        // init field classification
        fieldAdapter.classifyFields(CACHEABLE_FIELDS, array(userTextField, passwordField, tokenConfigFileTextField, tokenProfileComboBox));
    }


    private void updateAuthenticationFields() {
        DBNFormFieldAdapter fieldAdapter = getFieldAdapter();

        // cache values of fields classified as CACHEABLE
        fieldAdapter.captureFieldValues(classifiedAs(CACHEABLE_FIELDS));
        fieldAdapter.updateFieldsVisibility();
        fieldAdapter.updateFieldsAccessibility();
        fieldAdapter.resetFieldValues(inaccessible());

        // restore values of fields classified as CACHEABLE which are visible and enabled
        fieldAdapter.restoreFieldValues(accessibleClassifiedAs(CACHEABLE_FIELDS));
    }

    public String getUser() {
        return userTextField.getText();
    }

    public void applyFormChanges(AuthenticationInfo authenticationInfo){
        // irrelevant fields are all supposed to be emptied at this stage by resetFieldValues(), if disabled or hidden
        // no auth type check needed here
        authenticationInfo.setType(getSelection(authTypeComboBox));
        authenticationInfo.setUser(userTextField.getText());
        authenticationInfo.setPassword(new String(passwordField.getPassword()));

        authenticationInfo.setTokenBrowserAuth(tokenBrowserAuthCheckBox.isSelected());
        authenticationInfo.setTokenConfigFile(tokenConfigFileTextField.getText());
        authenticationInfo.setTokenProfile(getSelection(tokenProfileComboBox));
    }

    public void resetFormChanges() {
        ConnectionDatabaseSettingsForm parent = ensureParentComponent();
        ConnectionDatabaseSettings configuration = parent.getConfiguration();
        AuthenticationInfo authenticationInfo = configuration.getAuthenticationInfo();

        userTextField.setText(authenticationInfo.getUser());
        passwordField.setText(authenticationInfo.getPassword());
        setSelection(authTypeComboBox, authenticationInfo.getType());

        tokenConfigFileTextField.setText(authenticationInfo.getTokenConfigFile());
        setSelection(tokenProfileComboBox, authenticationInfo.getTokenProfile());
        tokenBrowserAuthCheckBox.setSelected(authenticationInfo.isTokenBrowserAuth());
        updateAuthenticationFields();
    }

    private void refreshTokenProfileOptions() {
        JTextField textField = tokenConfigFileTextField.getTextField();
        String configFilePath = textField.getText();
        List<String> profiles = Collections.emptyList();
        try {
            // TODO this may take time to load if file is located on a remote location - consider showing a spinner next to the profile dropdown
            //  (is remote config a valid use case anyways?)
            profiles = loadTokenProfiles(configFilePath);
            TextFields.updateFieldError(textField, null);
        } catch (Exception e) {
            TextFields.updateFieldError(textField, e.getMessage());
        }
    	tokenProfileComboBox.setModel(new DefaultComboBoxModel<>(profiles.toArray(new String[0])));
    }

	private List<String> loadTokenProfiles(String configFilePath) {
        if (configFilePath == null) return Collections.emptyList();

        File configFile = new File(configFilePath);
        if (!configFile.exists()) throw new IllegalArgumentException("File does not exist");
        if (!configFile.isFile()) throw new IllegalArgumentException("Path is expected to be a config file");

		List<String> profileEntries = new ArrayList<>();
		try (FileReader fileReader =  new FileReader(configFile);
			    BufferedReader configReader = new BufferedReader(fileReader);)
		{
			String nextLine;
			while ((nextLine = configReader.readLine()) != null) {
				nextLine = nextLine.trim();
                // TODO maybe use regex "\[[a-zA-Z0-9-]+\]"
				if (nextLine.length() > 2) {  // must be '[' and  ']' plus at least on char
					char firstChar = nextLine.charAt(0);
					if (firstChar == '[') {
						final int lastCharIdx = nextLine.length()-1;
						char lastChar = nextLine.charAt(lastCharIdx);
						if (lastChar == ']') {
							// apparently the ConfigParser accepts everything.
							// should we be more protective?
							profileEntries.add(nextLine.substring(1, lastCharIdx));
						}
					}
				}
			}
            if (profileEntries.isEmpty()) throw new IllegalArgumentException("No profile entries found in the given file");
		}
		catch (IOException ioe) {
            throw new IllegalArgumentException("Failed to load config file. Cause: " + ioe.getMessage());
		}

        return profileEntries;
	}

    /***********************************************************************
     *                          LOOKUP UTILITIES                           *
     ***********************************************************************/

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public @Nullable String getTokenConfigFile() {
        return tokenConfigFileTextField.getText();
    }

    public @Nullable String getTokenProfile() {
        return (String) tokenProfileComboBox.getSelectedItem();
    }

    private boolean isUserAuth() {
        return Commons.isOneOf(getAuthenticationType(), USER, USER_PASSWORD);
    }

    private boolean isPasswordAuth() {
        return getAuthenticationType() == USER_PASSWORD;
    }

    private boolean isTokenAuth() {
        return getAuthenticationType() == AuthenticationType.TOKEN;
    }

    private boolean isBrowserTokenAuth() {
        return isTokenAuth() && tokenBrowserAuthCheckBox.isSelected();
    }

    @Nullable
    private AuthenticationType getAuthenticationType() {
        return (AuthenticationType) authTypeComboBox.getSelectedItem();
    }

}
