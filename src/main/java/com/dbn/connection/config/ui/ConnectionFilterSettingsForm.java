package com.dbn.connection.config.ui;

import com.dbn.browser.options.ObjectFilterChangeListener;
import com.dbn.common.constant.Constant;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.options.SettingsChangeNotifier;
import com.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ConnectionFilterSettings;
import com.dbn.object.type.DBObjectType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ConnectionFilterSettingsForm extends CompositeConfigurationEditorForm<ConnectionFilterSettings> {
    private JPanel mainPanel;
    private JPanel objectTypesFilterPanel;
    private JPanel objectCustomFiltersPanel;
    private JCheckBox hideEmptySchemasCheckBox;
    private JCheckBox hideAuditColumnsCheckBox;
    private JCheckBox hidePseudoColumnsCheckBox;

    public ConnectionFilterSettingsForm(ConnectionFilterSettings settings) {
        super(settings);
        objectCustomFiltersPanel.add(settings.getObjectFilterSettings().createComponent(), BorderLayout.CENTER);
        objectTypesFilterPanel.add(settings.getObjectTypeFilterSettings().createComponent(), BorderLayout.CENTER);

        hideEmptySchemasCheckBox.setSelected(settings.isHideEmptySchemas());
        hideAuditColumnsCheckBox.setSelected(settings.isHideAuditColumns());
        hidePseudoColumnsCheckBox.setSelected(settings.isHidePseudoColumns());

        registerComponent(hideEmptySchemasCheckBox);
        registerComponent(hideAuditColumnsCheckBox);
        registerComponent(hidePseudoColumnsCheckBox);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        ConnectionFilterSettings configuration = getConfiguration();
        boolean notifyFilterListenersSchemas = configuration.isHideEmptySchemas() != hideEmptySchemasCheckBox.isSelected();
        boolean notifyFilterListenersColumns =
                configuration.isHideAuditColumns() != hideAuditColumnsCheckBox.isSelected() ||
                configuration.isHidePseudoColumns() != hidePseudoColumnsCheckBox.isSelected();

        applyFormChanges(configuration);

        Project project = configuration.getProject();
        SettingsChangeNotifier.register(() -> {
            ConnectionId connectionId = configuration.getConnectionId();
            if (notifyFilterListenersSchemas) {
                ProjectEvents.notify(project,
                        ObjectFilterChangeListener.TOPIC,
                        (listener) -> listener.nameFiltersChanged(connectionId, Constant.array(DBObjectType.SCHEMA)));
            }
            if (notifyFilterListenersColumns) {
                ProjectEvents.notify(project,
                    ObjectFilterChangeListener.TOPIC,
                    (listener) -> listener.nameFiltersChanged(connectionId, Constant.array(DBObjectType.COLUMN)));
            }
        });
    }

    @Override
    public void applyFormChanges(ConnectionFilterSettings configuration) throws ConfigurationException {
        configuration.setHideEmptySchemas(hideEmptySchemasCheckBox.isSelected());
        configuration.setHideAuditColumns(hideAuditColumnsCheckBox.isSelected());
        configuration.setHidePseudoColumns(hidePseudoColumnsCheckBox.isSelected());
    }
}
