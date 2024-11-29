/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.connection.config;

import com.dbn.common.options.BasicProjectConfiguration;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ui.ConnectionSshTunnelSettingsForm;
import com.dbn.connection.ssh.SshAuthType;
import com.dbn.credentials.DatabaseCredentialManager;
import com.dbn.credentials.Secret;
import com.dbn.credentials.SecretHolder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.options.setting.Settings.getBoolean;
import static com.dbn.common.options.setting.Settings.getEnum;
import static com.dbn.common.options.setting.Settings.getString;
import static com.dbn.common.options.setting.Settings.setBoolean;
import static com.dbn.common.options.setting.Settings.setEnum;
import static com.dbn.common.options.setting.Settings.setString;
import static com.dbn.common.util.Strings.isEmpty;
import static com.dbn.common.util.Strings.isNotEmpty;
import static com.dbn.credentials.SecretType.SSH_TUNNEL_PASSPHRASE;
import static com.dbn.credentials.SecretType.SSH_TUNNEL_PASSWORD;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ConnectionSshTunnelSettings extends BasicProjectConfiguration<ConnectionSettings, ConnectionSshTunnelSettingsForm> implements SecretHolder {
     // TODO passwords moved to IDE keychain (cleanup after followup release)
    @Deprecated private static final String DEPRECATED_PWD_ATTRIBUTE = "deprecated-proxy-pwd";
    @Deprecated private static final String DEPRECATED_PASSPHRASE_ATTRIBUTE = "key-passphrase";

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

        authType = getEnum(element, "auth-type", authType);
        keyFile = getString(element, "key-file", keyFile);

        if (isTransientContext()) {
            // only propagate password when config context is transient
            // (avoid storing it in config xml)
            password = getString(element, "transient-password", password);
            keyPassphrase = getString(element, "transient-key-passphrase", keyPassphrase);
        }

        restorePasswords(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "active", active);
        setString(element, "proxy-host", host);
        setString(element, "proxy-port", port);
        setString(element, "proxy-user", user);
        setEnum(element, "auth-type", authType);
        setString(element, "key-file", keyFile);

        if (isTransientContext()) {
            // only propagate password when config context is transient
            // (avoid storing it in config xml)
            setString(element, "transient-password", password);
            setString(element, "transient-key-passphrase", keyPassphrase);
        }
    }

    public ConnectionId getConnectionId() {
        return ensureParent().getConnectionId();
    }

    @Deprecated // TODO cleanup in subsequent release (temporarily support old storage)
    private void restorePasswords(Element element) {
        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
        if (authType == SshAuthType.PASSWORD && isEmpty(password)) {
            password = Passwords.decodePassword(getString(element, DEPRECATED_PWD_ATTRIBUTE, password));
            // password still in old config store
            if (isNotEmpty(user) && isNotEmpty(password)) {
                credentialManager.uploadSecret(getConnectionId(), createPasswordSecret());
            }
        }
        if (authType == SshAuthType.KEY_PAIR && isEmpty(keyPassphrase)) {
            keyPassphrase = Passwords.decodePassword(getString(element, DEPRECATED_PASSPHRASE_ATTRIBUTE, keyPassphrase));
            // password still in old config store
            if (isNotEmpty(keyFile) && isNotEmpty(keyPassphrase)) {
                credentialManager.uploadSecret(getConnectionId(), createKeyPassphraseSecret());
            }
        }
    }


    /*********************************************************
     *                     SecretHolder                      *
     *********************************************************/

    @Override
    public @NotNull Object getSecretId() {
        return getConnectionId();
    }

    @Override
    public Secret[] createSecrets() {
        return new Secret[] {
                createPasswordSecret(),
                createKeyPassphraseSecret()};
    }

    private Secret createPasswordSecret() {
        return new Secret(SSH_TUNNEL_PASSWORD, user, password);
    }

    private Secret createKeyPassphraseSecret() {
        return new Secret(SSH_TUNNEL_PASSPHRASE, keyFile, keyPassphrase );
    }

    /**
     * Load password or passphrase from Password Safe
     */
    public void initSecrets() {
        if (!active) return;

        ConnectionId connectionId = getConnectionId();
        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
        if (authType == SshAuthType.PASSWORD) {
            Secret secret = credentialManager.loadSecret(SSH_TUNNEL_PASSWORD, connectionId, user);
            password = secret.getStringToken();
        } else if (authType == SshAuthType.KEY_PAIR) {
            Secret secret = credentialManager.loadSecret(SSH_TUNNEL_PASSPHRASE, connectionId, keyFile);
            keyPassphrase = secret.getStringToken();
        }
    }
}
