package com.dbn.editor.data.options.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.options.ui.ConfigurationEditors;
import com.dbn.editor.data.options.DataEditorGeneralSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DataEditorGeneralSettingsForm extends ConfigurationEditorForm<DataEditorGeneralSettings> {
    private JPanel mainPanel;
    private JTextField fetchBlockSizeTextField;
    private JTextField fetchTimeoutTextField;
    private JCheckBox trimWhitespacesCheckBox;
    private JCheckBox convertEmptyToNullCheckBox;
    private JCheckBox selectContentOnEditCheckBox;
    private JCheckBox largeValuePreviewActiveCheckBox;

    public DataEditorGeneralSettingsForm(DataEditorGeneralSettings settings) {
        super(settings);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        ConfigurationEditors.validateIntegerValue(fetchBlockSizeTextField, "Fetch block size", true, 1, 10000, null);
        ConfigurationEditors.validateIntegerValue(fetchTimeoutTextField, "Fetch timeout", true, 0, 300, "\nUse value 0 for no timeout");

        DataEditorGeneralSettings settings = getConfiguration();
        settings.getFetchBlockSize().to(fetchBlockSizeTextField);
        settings.getFetchTimeout().to(fetchTimeoutTextField);
        settings.getTrimWhitespaces().to(trimWhitespacesCheckBox);
        settings.getConvertEmptyStringsToNull().to(convertEmptyToNullCheckBox);
        settings.getSelectContentOnCellEdit().to(selectContentOnEditCheckBox);
        settings.getLargeValuePreviewActive().to(largeValuePreviewActiveCheckBox);
    }

    @Override
    public void resetFormChanges() {
        DataEditorGeneralSettings settings = getConfiguration();
        settings.getFetchBlockSize().from(fetchBlockSizeTextField);
        settings.getFetchTimeout().from(fetchTimeoutTextField);
        settings.getTrimWhitespaces().from(trimWhitespacesCheckBox);
        settings.getConvertEmptyStringsToNull().from(convertEmptyToNullCheckBox);
        settings.getSelectContentOnCellEdit().from(selectContentOnEditCheckBox);
        settings.getLargeValuePreviewActive().from(largeValuePreviewActiveCheckBox);
    }
}
