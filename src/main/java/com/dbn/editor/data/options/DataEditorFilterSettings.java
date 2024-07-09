package com.dbn.editor.data.options;

import com.dbn.common.options.BasicConfiguration;
import com.dbn.common.options.setting.Settings;
import com.dbn.editor.data.filter.DatasetFilterType;
import com.dbn.editor.data.options.ui.DataEditorFilterSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DataEditorFilterSettings extends BasicConfiguration<DataEditorSettings, DataEditorFilterSettingsForm> {
    private boolean promptFilterDialog = true;
    private DatasetFilterType defaultFilterType = DatasetFilterType.BASIC;

    DataEditorFilterSettings(DataEditorSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.dataEditor.title.FilterSettings");
    }

    @Override
    public String getHelpTopic() {
        return "dataEditor";
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public DataEditorFilterSettingsForm createConfigurationEditor() {
        return new DataEditorFilterSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "filters";
    }

    @Override
    public void readConfiguration(Element element) {
        promptFilterDialog = Settings.getBoolean(element, "prompt-filter-dialog", promptFilterDialog);
        defaultFilterType = Settings.getEnum(element, "default-filter-type", defaultFilterType);
    }

    @Override
    public void writeConfiguration(Element element) {
        Settings.setBoolean(element, "prompt-filter-dialog", promptFilterDialog);
        Settings.setEnum(element, "default-filter-type", defaultFilterType);
    }
}
