package com.dbn.editor.data.options;

import com.dbn.common.options.BasicConfiguration;
import com.dbn.common.options.setting.Settings;
import com.dbn.editor.data.options.ui.DatatEditorValueListPopupSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DataEditorValueListPopupSettings extends BasicConfiguration<DataEditorSettings, DatatEditorValueListPopupSettingsForm> {
    private boolean showPopupButton = true;
    private int elementCountThreshold = 1000;
    private int dataLengthThreshold = 250;

    DataEditorValueListPopupSettings(DataEditorSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.dataEditor.title.ValueLists");
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
    public DatatEditorValueListPopupSettingsForm createConfigurationEditor() {
        return new DatatEditorValueListPopupSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "values-actions-popup";
    }

    @Override
    public void readConfiguration(Element element) {
        showPopupButton = Settings.getBoolean(element, "show-popup-button", showPopupButton);
        elementCountThreshold = Settings.getInteger(element, "element-count-threshold", elementCountThreshold);
        dataLengthThreshold = Settings.getInteger(element, "data-length-threshold", dataLengthThreshold);
    }

    @Override
    public void writeConfiguration(Element element) {
        Settings.setBoolean(element, "show-popup-button", showPopupButton);
        Settings.setInteger(element, "element-count-threshold", elementCountThreshold);
        Settings.setInteger(element, "data-length-threshold", dataLengthThreshold);
    }
}
