package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.editor.data.options.DataEditorGeneralSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DataEditorGeneralSettingsForm extends ConfigurationEditorForm<DataEditorGeneralSettings> {
    private JPanel mainPanel;
    private JTextField fetchBlockSizeTextField;
    private JTextField fetchTimeoutTextField;
    private JCheckBox trimWhitespacesCheckBox;
    private JCheckBox convertEmptyToNullCheckBox;
    private JCheckBox selectContentOnEditCheckBox;
    private JCheckBox largeValuePreviewActiveCheckBox;
    private JCheckBox loadViewDataActiveCheckBox;

    public DataEditorGeneralSettingsForm(DataEditorGeneralSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        ConfigurationEditorUtil.validateIntegerInputValue(fetchBlockSizeTextField, "Fetch block size", true, 1, 10000, null);
        ConfigurationEditorUtil.validateIntegerInputValue(fetchTimeoutTextField, "Fetch timeout", true, 0, 300, "\nUse value 0 for no timeout");

        DataEditorGeneralSettings settings = getConfiguration();
        settings.getFetchBlockSize().to(fetchBlockSizeTextField);
        settings.getFetchTimeout().to(fetchTimeoutTextField);
        settings.getTrimWhitespaces().to(trimWhitespacesCheckBox);
        settings.getConvertEmptyStringsToNull().to(convertEmptyToNullCheckBox);
        settings.getSelectContentOnCellEdit().to(selectContentOnEditCheckBox);
        settings.getLargeValuePreviewActive().to(largeValuePreviewActiveCheckBox);
        settings.getLoadViewDataActive().to(loadViewDataActiveCheckBox);
    }

    public void resetFormChanges() {
        DataEditorGeneralSettings settings = getConfiguration();
        settings.getFetchBlockSize().from(fetchBlockSizeTextField);
        settings.getFetchTimeout().from(fetchTimeoutTextField);
        settings.getTrimWhitespaces().from(trimWhitespacesCheckBox);
        settings.getConvertEmptyStringsToNull().from(convertEmptyToNullCheckBox);
        settings.getSelectContentOnCellEdit().from(selectContentOnEditCheckBox);
        settings.getLargeValuePreviewActive().from(largeValuePreviewActiveCheckBox);
        settings.getLoadViewDataActive().from(loadViewDataActiveCheckBox);
    }
}
