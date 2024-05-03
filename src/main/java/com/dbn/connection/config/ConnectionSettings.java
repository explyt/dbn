package com.dbn.connection.config;

import com.dbn.common.options.CompositeProjectConfiguration;
import com.dbn.common.options.Configuration;
import com.dbn.common.property.PropertyHolderBase;
import com.dbn.common.util.Cloneable;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.DatabaseType;
import com.dbn.connection.config.ui.ConnectionSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.dbn.common.options.setting.Settings.booleanAttribute;
import static com.dbn.common.options.setting.Settings.connectionIdAttribute;
import static com.dbn.connection.config.ConnectionSettingsStatus.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ConnectionSettings extends CompositeProjectConfiguration<ConnectionBundleSettings, ConnectionSettingsForm>
        implements Cloneable<ConnectionSettings> {

    private ConnectionId connectionId;

    private final PropertyHolderBase<ConnectionSettingsStatus> status = new PropertyHolderBase.IntStore<ConnectionSettingsStatus>(ACTIVE, SIGNED) {
        @Override
        protected ConnectionSettingsStatus[] properties() {
            return ConnectionSettingsStatus.VALUES;
        }
    };

    private final ConnectionDatabaseSettings databaseSettings;
    private final @Getter(lazy = true) ConnectionPropertiesSettings propertiesSettings = new ConnectionPropertiesSettings(this);
    private final @Getter(lazy = true) ConnectionSshTunnelSettings sshTunnelSettings   = new ConnectionSshTunnelSettings(this);
    private final @Getter(lazy = true) ConnectionSslSettings sslSettings               = new ConnectionSslSettings(this);
    private final @Getter(lazy = true) ConnectionDetailSettings detailSettings         = new ConnectionDetailSettings(this);
    private final @Getter(lazy = true) ConnectionDebuggerSettings debuggerSettings     = new ConnectionDebuggerSettings(this);
    private final @Getter(lazy = true) ConnectionFilterSettings filterSettings         = new ConnectionFilterSettings(this);

    public ConnectionSettings(ConnectionBundleSettings parent) {
        this(parent, DatabaseType.GENERIC, ConnectionConfigType.CUSTOM);
    }

    public ConnectionSettings(ConnectionBundleSettings parent, DatabaseType databaseType, ConnectionConfigType configType) {
        super(parent);
        databaseSettings = new ConnectionDatabaseSettings(this, databaseType, configType);
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                getDatabaseSettings(),
                getPropertiesSettings(),
                getSshTunnelSettings(),
                getSslSettings(),
                getDetailSettings(),
                getDebuggerSettings(),
                getFilterSettings()};
    }

    public void generateNewId() {
        connectionId = ConnectionId.create();
    }

    @NotNull
    @Override
    public ConnectionSettingsForm createConfigurationEditor() {
        return new ConnectionSettingsForm(this);
    }

    public boolean isNew() {
        return status.is(NEW);
    }

    public boolean isActive() {
        return status.is(ACTIVE);
    }

    public boolean isSigned() {
        return status.is(SIGNED);
    }

    public void setNew(boolean isNew) {
        status.set(NEW, isNew);
    }

    public void setActive(boolean active) {
        status.set(ACTIVE, active);
    }

    public void setSigned(boolean signed) {
        status.set(SIGNED, signed);
    }

    @Override
    public void readConfiguration(Element element) {
        if (ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.get()) {
            generateNewId();
        } else {
            connectionId = connectionIdAttribute(element, "id");
        }
        status.set(ACTIVE, booleanAttribute(element, "active", true));
        status.set(SIGNED, booleanAttribute(element, "signed", true));
        super.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("id", connectionId.id());
        element.setAttribute("active", Boolean.toString(isActive()));
        element.setAttribute("signed", Boolean.toString(isSigned()));
        super.writeConfiguration(element);
    }

    @Override
    public ConnectionSettings clone() {
        Element element = new Element("Connection");
        writeConfiguration(element);
        ConnectionDatabaseSettings databaseSettings = getDatabaseSettings();
        ConnectionSettings clone = new ConnectionSettings(getParent() /*TODO config*/, databaseSettings.getDatabaseType(), databaseSettings.getConfigType());
        clone.readConfiguration(element);
        clone.getDatabaseSettings().setConnectivityStatus(databaseSettings.getConnectivityStatus());
        clone.generateNewId();
        return clone;
    }

    @Override
    public String toString() {
        return Objects.toString(connectionId);
    }
}
