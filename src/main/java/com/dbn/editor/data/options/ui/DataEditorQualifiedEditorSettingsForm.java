package com.dbn.editor.data.options.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.ui.list.CheckBoxList;
import com.dbn.editor.data.options.DataEditorQualifiedEditorSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.common.options.ui.ConfigurationEditors.validateIntegerValue;

public class DataEditorQualifiedEditorSettingsForm extends ConfigurationEditorForm<DataEditorQualifiedEditorSettings> {
    private JPanel mainPanel;
    private JScrollPane listScrollPane;
    private JTextField textLengthThresholdTextField;
    private CheckBoxList checkBoxList;

    public DataEditorQualifiedEditorSettingsForm(DataEditorQualifiedEditorSettings settings) {
        super(settings);
        checkBoxList = new CheckBoxList(settings.getContentTypes());
        listScrollPane.setViewportView(checkBoxList);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        DataEditorQualifiedEditorSettings settings = getConfiguration();
        checkBoxList.applyChanges();
        settings.setTextLengthThreshold(validateIntegerValue(
                textLengthThresholdTextField,
                "Text Length Threshold", true, 0, 999999999, null));
    }

    @Override
    public void resetFormChanges() {
        DataEditorQualifiedEditorSettings settings = getConfiguration();
        textLengthThresholdTextField.setText(Integer.toString(settings.getTextLengthThreshold()));
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
