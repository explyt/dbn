package com.dbn.connection.config.ui;

import com.dbn.common.constant.Constants;
import com.dbn.common.database.AuthenticationInfo;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.util.Strings;
import com.dbn.connection.AuthenticationType;
import com.dbn.connection.config.ConnectionDatabaseSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;

import static com.dbn.common.ui.util.ComboBoxes.*;

public class ConnectionAuthenticationSettingsForm extends DBNFormBase {
    private JComboBox<AuthenticationType> authTypeComboBox;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JPanel mainPanel;
    private JLabel userLabel;
    private JLabel passwordLabel;

    private String cachedUser = "";
    private String cachedPassword = "";

    private final ActionListener actionListener = e -> updateAuthenticationFields();

    ConnectionAuthenticationSettingsForm(@NotNull ConnectionDatabaseSettingsForm parentComponent) {
        super(parentComponent);
        initComboBox(authTypeComboBox, AuthenticationType.values());
        authTypeComboBox.addActionListener(actionListener);
    }

    private void updateAuthenticationFields() {
        AuthenticationType authType = getSelection(authTypeComboBox);

        boolean showUser = Constants.isOneOf(authType,
                AuthenticationType.USER,
                AuthenticationType.USER_PASSWORD);
        boolean showPassword = authType == AuthenticationType.USER_PASSWORD;


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
    }

    public String getUser() {
        return userTextField.getText();
    }

    public void applyFormChanges(AuthenticationInfo authenticationInfo){
        authenticationInfo.setType(getSelection(authTypeComboBox));
        authenticationInfo.setUser(userTextField.getText());
        authenticationInfo.setPassword(new String(passwordField.getPassword()));
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
        updateAuthenticationFields();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
