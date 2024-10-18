package com.dbn.connection.config.ui;

import com.dbn.common.constant.Constants;
import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.ui.Presentable;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.util.Strings;
import com.dbn.connection.AuthenticationType;
import com.dbn.connection.config.ConnectionDatabaseSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.dbn.common.ui.util.ComboBoxes.*;
import static com.dbn.common.ui.util.TextFields.onTextChange;

import com.dbn.common.ui.util.TextFields.*; 
import com.intellij.openapi.ui.*;

public class ConnectionAuthenticationSettingsForm extends DBNFormBase {
    private JComboBox<AuthenticationType> authTypeComboBox;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JPanel mainPanel;
    private JLabel userLabel;
    private JLabel passwordLabel;
    private JCheckBox useBrowserToAuthenticateCheckBox;
    private TextFieldWithBrowseButton configFileFieldWithBrowse;
    private JComboBox<String> profileComboDropDown;
    private JPanel tokenAuthPanel;
    private JTextField textField2;
    private JButton customProviderLIbraryButton;
    private JLabel tokenAuthLabel;

    private String cachedUser = "";
    private String cachedPassword = "";

    private final ActionListener actionListener = e -> updateAuthenticationFields();

    ConnectionAuthenticationSettingsForm(@NotNull ConnectionDatabaseSettingsForm parentComponent) {
        super(parentComponent);
        
        configFileFieldWithBrowse.addBrowseFolderListener(
                "Select Wallet Directory",
                "Folder must contain tnsnames.ora",
                null, new FileChooserDescriptor(true, false, false, false, false, false));
        onTextChange(configFileFieldWithBrowse, e -> handleConfigFileChanged(configFileFieldWithBrowse.getText()));
        
        initComboBox(authTypeComboBox, AuthenticationType.values());
        authTypeComboBox.addActionListener(actionListener);
        useBrowserToAuthenticateCheckBox.addActionListener(actionListener);
        useBrowserToAuthenticateCheckBox.setVisible(false);
    }

    private void updateAuthenticationFields() {
        AuthenticationType authType = getSelection(authTypeComboBox);

        boolean showUser = Constants.isOneOf(authType,
                AuthenticationType.USER,
                AuthenticationType.USER_PASSWORD);
        boolean showPassword = authType == AuthenticationType.USER_PASSWORD;

        boolean showTokenAuthentication = (authType == AuthenticationType.TOKEN_AUTHENTICATION);
        boolean useBrowserAuth = this.useBrowserToAuthenticateCheckBox.isSelected();

        userLabel.setVisible(showUser);
        userTextField.setVisible(showUser);

        passwordLabel.setVisible(showPassword);
        passwordField.setVisible(showPassword);
        //passwordField.setBackground(showPasswordField ? UIUtil.getTextFieldBackground() : UIUtil.getPanelBackground());

        String user = userTextField.getText();
        String password = new String(passwordField.getPassword());
        if (Strings.isNotEmpty(user)) cachedUser = user;
        if (Strings.isNotEmpty(password)) cachedPassword = password;

        userTextField.setText(showUser ? cachedUser : "");
        passwordField.setText(showPassword ? cachedPassword : "");

        tokenAuthPanel.setVisible(showTokenAuthentication);
        configFileFieldWithBrowse.setEnabled(!useBrowserAuth);
        profileComboDropDown.setEnabled(!useBrowserAuth);

        String configFilePath = configFileFieldWithBrowse.getText();
        Optional<String> selectedItem = Optional.ofNullable((String)profileComboDropDown.getSelectedItem());
		String profileName = selectedItem.orElse(null);
        boolean isUseBrowser = useBrowserToAuthenticateCheckBox.isSelected();

        if (authType == AuthenticationType.TOKEN_AUTHENTICATION) {
            useBrowserToAuthenticateCheckBox.setSelected(isUseBrowser);
            if (!isUseBrowser) {
                configFileFieldWithBrowse.setText(configFilePath);
                profileComboDropDown.setSelectedItem(profileName);
            }
        }
    }

    public JTextField getUserTextField() {
        return userTextField;
    }

    public String getUser() {
        return userTextField.getText();
    }

    public void applyFormChanges(AuthenticationInfo authenticationInfo){
        authenticationInfo.setType(getSelection(authTypeComboBox));
        if (authenticationInfo.getType() != AuthenticationType.TOKEN_AUTHENTICATION) {
	        authenticationInfo.setUser(userTextField.getText());
	        authenticationInfo.setPassword(new String(passwordField.getPassword()));
        }
        else {
        	authenticationInfo.setUseBrowserForTokenAuth(useBrowserToAuthenticateCheckBox.isSelected());
        	authenticationInfo.setPathToConfigFile(configFileFieldWithBrowse.getText());
        	authenticationInfo.setProfile(getSelection(profileComboDropDown));
        }
    }

    public void resetFormChanges() {
        ConnectionDatabaseSettingsForm parent = ensureParentComponent();
        ConnectionDatabaseSettings configuration = parent.getConfiguration();
        AuthenticationInfo authenticationInfo = configuration.getAuthenticationInfo();


        String user = authenticationInfo.getUser();
        String password = authenticationInfo.getPassword();
        if (Strings.isNotEmpty(user)) cachedUser = user;
        if (Strings.isNotEmpty(password)) cachedPassword = password;

        userTextField.setText(authenticationInfo.getUser());
        passwordField.setText(authenticationInfo.getPassword());
        setSelection(authTypeComboBox, authenticationInfo.getType());
        String pathToConfigFile = authenticationInfo.getPathToConfigFile();
        // populate the combo if possible
        if (pathToConfigFile != null) {
        	configFileFieldWithBrowse.setText(pathToConfigFile);
            handleConfigFileChanged(pathToConfigFile);
        }
        setSelection(profileComboDropDown, authenticationInfo.getProfile());
        useBrowserToAuthenticateCheckBox.setSelected(authenticationInfo.isUseBrowserForTokenAuth());
        updateAuthenticationFields();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public @Nullable String getPathToConfigFile() {
        return configFileFieldWithBrowse.getText();
    }

    public @Nullable String getProfile() {
        return Optional.ofNullable((String)profileComboDropDown.getSelectedItem()).orElse(null);
    }
    
    private void handleConfigFileChanged(@NotNull String text) {
    	profileComboDropDown.setModel(new DefaultComboBoxModel<String>(new String[0]));
        File configFile = new File(text);
        if (!configFile.isFile()) return;
        if (!configFile.exists()) return;

        List<String> profiles = getProfileEntries(configFile);
        profileComboDropDown.setModel(new DefaultComboBoxModel<String>(profiles.toArray(new String[0])));
    }

	private List<String> getProfileEntries(File configFile) {
		List<String> profileEntries = new ArrayList<>();
		try (FileReader fileReader =  new FileReader(configFile);
			    BufferedReader configReader = new BufferedReader(fileReader);)
		{
			String nextLine;
			while ((nextLine = configReader.readLine()) != null) {
				nextLine = nextLine.trim();
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
		}
		catch (IOException ioe) {
			// TODO: what logging to use?
			ioe.printStackTrace();
		}
		return profileEntries;
	}
}
