package com.dbn.data.grid.options.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.options.ui.ConfigurationEditors;
import com.dbn.data.grid.options.DataGridSortingSettings;
import com.dbn.data.grid.options.NullSortingOption;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.common.ui.util.ComboBoxes.*;

public class DataGridSortingSettingsForm extends ConfigurationEditorForm<DataGridSortingSettings> {
    private JPanel mainPanel;
    private JCheckBox enableZoomingCheckBox;
    private JTextField maxSortingColumnsTextField;
    private JComboBox<NullSortingOption> nullsPositionComboBox;

    public DataGridSortingSettingsForm(DataGridSortingSettings settings) {
        super(settings);
        initComboBox(nullsPositionComboBox, NullSortingOption.values());

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
        DataGridSortingSettings settings = getConfiguration();
        settings.setNullsFirst(getSelection(nullsPositionComboBox) == NullSortingOption.FIRST);
        int maxSortingColumns = ConfigurationEditors.validateIntegerValue(maxSortingColumnsTextField, "Max sorting columns", true, 0, 100, "Use value 0 for unlimited number of sorting columns");
        settings.setMaxSortingColumns(maxSortingColumns);
    }

    @Override
    public void resetFormChanges() {
        DataGridSortingSettings settings = getConfiguration();
        setSelection(nullsPositionComboBox, settings.isNullsFirst() ? NullSortingOption.FIRST : NullSortingOption.LAST);
        maxSortingColumnsTextField.setText(Integer.toString(settings.getMaxSortingColumns()));
    }
}
