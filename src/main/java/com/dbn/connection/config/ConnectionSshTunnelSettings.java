package com.dbn.connection.config;

import com.dbn.common.options.BasicProjectConfiguration;
import com.dbn.common.util.Strings;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ui.ConnectionSshTunnelSettingsForm;
import com.dbn.connection.ssh.SshAuthType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.options.setting.Settings.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ConnectionSshTunnelSettings extends BasicProjectConfiguration<ConnectionSettings, ConnectionSshTunnelSettingsForm> {
    @Deprecated // TODO move to keychain
    private static final String OLD_PWD_ATTRIBUTE = "proxy-password";
    @Deprecated // TODO move to keychain
    private static final String TEMP_PWD_ATTRIBUTE = "deprecated-proxy-pwd";

    private boolean active = false;
    private String host;
    private String user;
    private String password;
    private String port = "22";
    private SshAuthType authType = SshAuthType.PASSWORD;
    private String keyFile;
    private String keyPassphrase;

    ConnectionSshTunnelSettings(ConnectionSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.connection.title.SshTunnelSettings");
    }

    @Override
    public String getHelpTopic() {
        return "connectionSshTunnelSettings";
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @NotNull
    @Override
    public ConnectionSshTunnelSettingsForm createConfigurationEditor() {
        return new ConnectionSshTunnelSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "ssh-settings";
    }

    @Override
    public void readConfiguration(Element element) {
        active = getBoolean(element, "active", active);
        host = getString(element, "proxy-host", host);
        port = getString(element, "proxy-port", port);
        user = getString(element, "proxy-user", user);


        password = Passwords.decodePassword(getString(element, TEMP_PWD_ATTRIBUTE, password));
        if (Strings.isEmpty(password)) {
            password = Passwords.decodePassword(getString(element, OLD_PWD_ATTRIBUTE, password));
        }

        authType = getEnum(element, "auth-type", authType);
        keyFile = getString(element, "key-file", keyFile);
        keyPassphrase = Passwords.decodePassword(getString(element, "key-passphrase", keyPassphrase));
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "active", active);
        setString(element, "proxy-host", host);
        setString(element, "proxy-port", port);
        setString(element, "proxy-user", user);
        setString(element, TEMP_PWD_ATTRIBUTE, Passwords.encodePassword(password));
        setEnum(element, "auth-type", authType);
        setString(element, "key-file", keyFile);
        setString(element, "key-passphrase", Passwords.encodePassword(keyPassphrase));
    }

    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }
}
