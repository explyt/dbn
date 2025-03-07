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
import com.dbn.common.options.ConfigMonitor;
import com.dbn.common.util.Chars;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ui.ConnectionSshTunnelSettingsForm;
import com.dbn.connection.ssh.SshAuthType;
import com.dbn.credentials.DatabaseCredentialManager;
import com.dbn.credentials.Secret;
import com.dbn.credentials.SecretsOwner;
import com.dbn.credentials.SecretsOwnerRegistry;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.options.ConfigActivity.INITIALIZING;
import static com.dbn.common.options.setting.Settings.getBoolean;
import static com.dbn.common.options.setting.Settings.getChars;
import static com.dbn.common.options.setting.Settings.getEnum;
import static com.dbn.common.options.setting.Settings.getString;
import static com.dbn.common.options.setting.Settings.setBoolean;
import static com.dbn.common.options.setting.Settings.setChars;
import static com.dbn.common.options.setting.Settings.setEnum;
import static com.dbn.common.options.setting.Settings.setString;
import static com.dbn.common.util.Base64.decode;
import static com.dbn.common.util.Base64.encode;
import static com.dbn.common.util.Strings.isNotEmpty;
import static com.dbn.credentials.SecretType.SSH_TUNNEL_KEY_PASSPHRASE;
import static com.dbn.credentials.SecretType.SSH_TUNNEL_PASSWORD;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ConnectionSshTunnelSettings extends BasicProjectConfiguration<ConnectionSettings, ConnectionSshTunnelSettingsForm> implements SecretsOwner {
     // TODO passwords moved to IDE keychain (cleanup after followup release)
    @Deprecated private static final String DEPRECATED_PWD_ATTRIBUTE = "deprecated-proxy-pwd";
    @Deprecated private static final String DEPRECATED_PASSPHRASE_ATTRIBUTE = "key-passphrase";

    private boolean active = false;
    private String host;
    private String user;
    private char[] password;
    private String port = "22";
    private SshAuthType authType = SshAuthType.PASSWORD;
    private String keyFile;
    private char[] keyPassphrase;

    ConnectionSshTunnelSettings(ConnectionSettings parent) {
        super(parent);
        SecretsOwnerRegistry.register(this);
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
            password = decode(getChars(element, "transient-password", encode(password)));
            keyPassphrase = decode(getChars(element, "transient-key-passphrase", encode(keyPassphrase)));
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
            setChars(element, "transient-password", encode(password));
            setChars(element, "transient-key-passphrase", encode(keyPassphrase));
        }
    }

    public ConnectionId getConnectionId() {
        return ensureParent().getConnectionId();
    }

    @Deprecated // TODO cleanup in subsequent release (temporarily support old storage)
    private void restorePasswords(Element element) {
        if (!ConfigMonitor.is(INITIALIZING)) return; // only during config initialization
        if (!active) return;

        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
        if (authType == SshAuthType.PASSWORD) {
            if (Chars.isNotEmpty(password)) return; // do not overwrite

            password = decode(getChars(element, DEPRECATED_PWD_ATTRIBUTE, Chars.EMPTY_ARRAY));
            if (isNotEmpty(user) && Chars.isNotEmpty(password)) {
                // password still in old config store
                credentialManager.queueSecretsInsert(getConnectionId(), getPasswordSecret());
            }
        }

        if (authType == SshAuthType.KEY_PAIR) {
            if (Chars.isNotEmpty(keyPassphrase)) return; // do not overwrite

            keyPassphrase = decode(getChars(element, DEPRECATED_PASSPHRASE_ATTRIBUTE, Chars.EMPTY_ARRAY));
            if (isNotEmpty(keyFile) && Chars.isNotEmpty(keyPassphrase)) {
                // passphrase still in old config store
                credentialManager.queueSecretsInsert(getConnectionId(), getKeyPassphraseSecret());
            }
        }
    }


    /*********************************************************
     *                     SecretHolder                      *
     *********************************************************/

    @Override
    public @NotNull Object getSecretOwnerId() {
        return getConnectionId();
    }

    @Override
    public String getSecretOwnerName() {
        return ensureParent().getDatabaseSettings().getName();
    }

    @Override
    public Secret[] getSecrets() {
        return new Secret[] {
                getPasswordSecret(),
                getKeyPassphraseSecret()};
    }

    private Secret getPasswordSecret() {
        return new Secret(SSH_TUNNEL_PASSWORD, user, password);
    }

    private Secret getKeyPassphraseSecret() {
        return new Secret(SSH_TUNNEL_KEY_PASSPHRASE, keyFile, keyPassphrase);
    }

    /**
     * Load password or passphrase from Password Safe
     */
    @Override
    public void initSecrets() {
        if (!active) return;

        ConnectionId connectionId = getConnectionId();
        DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
        if (authType == SshAuthType.PASSWORD) {
            Secret secret = credentialManager.loadSecret(SSH_TUNNEL_PASSWORD, connectionId, user);
            password = secret.getToken();
        } else if (authType == SshAuthType.KEY_PAIR) {
            Secret secret = credentialManager.loadSecret(SSH_TUNNEL_KEY_PASSPHRASE, connectionId, keyFile);
            keyPassphrase = secret.getToken();
        }
    }
}
