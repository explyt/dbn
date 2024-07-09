package com.dbn.editor.data.options;

import com.dbn.common.options.BasicConfiguration;
import com.dbn.common.options.setting.Settings;
import com.dbn.editor.data.options.ui.DataEditorPopupSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DataEditorPopupSettings extends BasicConfiguration<DataEditorSettings, DataEditorPopupSettingsForm> {
    private boolean active = false;
    private boolean activeIfEmpty = false;
    private int dataLengthThreshold = 100;
    private int delay = 1000;

    public DataEditorPopupSettings(DataEditorSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.dataEditor.title.EditorPopups");
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
   @Override
   @NotNull
   public DataEditorPopupSettingsForm createConfigurationEditor() {
       return new DataEditorPopupSettingsForm(this);
   }

    @Override
    public String getConfigElementName() {
        return "text-editor-popup";
    }

    @Override
    public void readConfiguration(Element element) {
        active = Settings.getBoolean(element, "active", active);
        activeIfEmpty = Settings.getBoolean(element, "active-if-empty", activeIfEmpty);
        dataLengthThreshold = Settings.getInteger(element, "data-length-threshold", dataLengthThreshold);
        delay = Settings.getInteger(element, "popup-delay", delay);
    }

    @Override
    public void writeConfiguration(Element element) {
        Settings.setBoolean(element, "active", active);
        Settings.setBoolean(element, "active-if-empty", activeIfEmpty);
        Settings.setInteger(element, "data-length-threshold", dataLengthThreshold);
        Settings.setInteger(element, "popup-delay", delay);
    }

}
