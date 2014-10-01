package com.dci.intellij.dbn.connection.config;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.ProjectConfiguration;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerImpl;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.project.Project;

public class ConnectionBundleSettings extends ProjectConfiguration<ConnectionBundleSettingsForm> {
    private ConnectionBundle connectionBundle;
    public ConnectionBundleSettings(Project project) {
        super(project);
        connectionBundle = new ConnectionBundle(project);
    }

    public static ConnectionBundleSettings getInstance(Project project) {
        return ProjectSettingsManager.getSettings(project).getConnectionSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.ConnectionSettings";
    }

    public String getDisplayName() {
        return "Connections";
    }

    public String getHelpTopic() {
        return "connectionBundle";
    }

    @Override
    public String getConfigElementName() {
        return "connections";
    }

    public boolean isModified() {
        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
            if (connectionHandler.getSettings().isModified()) return true;
        }
        return false;
    }

    @Override
    protected Configuration<ConnectionBundleSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
    *                   UnnamedConfigurable                 *
    *********************************************************/
    public ConnectionBundleSettingsForm createConfigurationEditor() {
        return new ConnectionBundleSettingsForm(this);
    }

    /*********************************************************
     *                      Configurable                     *
     *********************************************************/
    public void readConfiguration(Element element) {
        for (Object o : element.getChildren()) {
            Element connectionElement = (Element) o;
            String connectionId = connectionElement.getAttributeValue("id");
            ConnectionHandler connectionHandler = null;
            if (connectionId != null) {
                connectionHandler = connectionBundle.getConnection(connectionId);
            }

            if (connectionHandler == null) {
                ConnectionSettings connectionSettings = new ConnectionSettings(this);
                connectionSettings.readConfiguration(connectionElement);
                connectionHandler = new ConnectionHandlerImpl(connectionBundle, connectionSettings);
                connectionBundle.addConnection(connectionHandler);
            } else {
                ConnectionSettings connectionSettings = connectionHandler.getSettings();
                connectionSettings.readConfiguration(connectionElement);
            }

            Element consolesElement = connectionElement.getChild("consoles");
            if (consolesElement != null) {
                for (Object c : consolesElement.getChildren()) {
                    Element consoleElement = (Element) c;
                    String consoleName = consoleElement.getAttributeValue("name");
                    connectionHandler.getConsoleBundle().createConsole(consoleName);
                }
            }
        }
    }

    public void writeConfiguration(Element element) {
        for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers().getFullList()) {
            Element connectionElement = new Element("connection");
            ConnectionSettings connectionSettings = connectionHandler.getSettings();
            connectionSettings.writeConfiguration(connectionElement);
            element.addContent(connectionElement);

            Element consolesElement = new Element("consoles");
            connectionElement.addContent(consolesElement);
            for (String consoleName : connectionHandler.getConsoleBundle().getConsoleNames()) {
                Element consoleElement = new Element("console");
                consoleElement.setAttribute("name", consoleName);
                consolesElement.addContent(consoleElement);
            }
        }
    }

    public ConnectionBundle getConnectionBundle() {
        return connectionBundle;
    }
}
