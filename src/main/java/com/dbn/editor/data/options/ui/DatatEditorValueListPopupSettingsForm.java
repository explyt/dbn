package com.dbn.editor.data.options.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.editor.data.options.DataEditorValueListPopupSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import static com.dbn.common.options.ui.ConfigurationEditors.validateIntegerValue;

public class DatatEditorValueListPopupSettingsForm extends ConfigurationEditorForm<DataEditorValueListPopupSettings> {
    private JTextField elementCountThresholdTextBox;
    private JTextField dataLengthThresholdTextBox;
    private JCheckBox showPopupButtonCheckBox;
    private JPanel mainPanel;

    public DatatEditorValueListPopupSettingsForm(DataEditorValueListPopupSettings settings) {
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
        DataEditorValueListPopupSettings settings = getConfiguration();
        settings.setShowPopupButton(showPopupButtonCheckBox.isSelected());
        settings.setElementCountThreshold(validateIntegerValue(elementCountThresholdTextBox, "Element count threshold", true, 0, 10000, null));
        settings.setDataLengthThreshold(validateIntegerValue(dataLengthThresholdTextBox, "Data length threshold", true, 0, 1000, null));
    }

    @Override
    public void resetFormChanges() {
        DataEditorValueListPopupSettings settings = getConfiguration();
        showPopupButtonCheckBox.setSelected(settings.isShowPopupButton());
        elementCountThresholdTextBox.setText(Integer.toString(settings.getElementCountThreshold()));
        dataLengthThresholdTextBox.setText(Integer.toString(settings.getDataLengthThreshold()));
    }
}
