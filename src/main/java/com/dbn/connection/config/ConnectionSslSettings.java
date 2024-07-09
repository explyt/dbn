package com.dbn.connection.config;

import com.dbn.common.options.BasicProjectConfiguration;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ui.ConnectionSslSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.options.setting.Settings.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ConnectionSslSettings extends BasicProjectConfiguration<ConnectionSettings, ConnectionSslSettingsForm> {
    private boolean active = false;
    private String certificateAuthorityFile;
    private String clientCertificateFile;
    private String clientKeyFile;

    ConnectionSslSettings(ConnectionSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.connection.title.SslSettings");
    }

    @Override
    public String getHelpTopic() {
        return "connectionSslSettings";
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @NotNull
    @Override
    public ConnectionSslSettingsForm createConfigurationEditor() {
        return new ConnectionSslSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "ssl-settings";
    }

    @Override
    public void readConfiguration(Element element) {
        active = getBoolean(element, "active", active);
        certificateAuthorityFile = getString(element, "certificate-authority-file", certificateAuthorityFile);
        clientCertificateFile = getString(element, "client-certificate-file", clientCertificateFile);
        clientKeyFile = getString(element, "client-key-file", clientKeyFile);
    }

    @Override
    public void writeConfiguration(Element element) {
        setBoolean(element, "active", active);
        setString(element, "certificate-authority-file", certificateAuthorityFile);
        setString(element, "client-certificate-file", clientCertificateFile);
        setString(element, "client-key-file", clientKeyFile);
    }

    public ConnectionId getConnectionId() {
        return getParent().getConnectionId();
    }
}
