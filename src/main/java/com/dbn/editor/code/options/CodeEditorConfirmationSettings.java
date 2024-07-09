package com.dbn.editor.code.options;

import com.dbn.common.option.ConfirmationOptionHandler;
import com.dbn.common.option.InteractiveOptionBroker;
import com.dbn.common.options.BasicConfiguration;
import com.dbn.editor.code.options.ui.CodeEditorConfirmationSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@EqualsAndHashCode(callSuper = false)
public class CodeEditorConfirmationSettings extends BasicConfiguration<CodeEditorSettings, CodeEditorConfirmationSettingsForm> {
    public static final String REMEMBER_OPTION_HINT = ""; //"\n\n(you can remember your option and change it at any time in Settings > Operations > Session Manager)";

    private final ConfirmationOptionHandler saveChanges =
            new ConfirmationOptionHandler(
                    "save-changes",
                    "Save changes",
                    "Save the changes for {0}?" + REMEMBER_OPTION_HINT, false);

    private final ConfirmationOptionHandler revertChanges =
            new ConfirmationOptionHandler(
                    "revert-changes",
                    "Revert Changes",
                    "Revert the changes for {0}?" + REMEMBER_OPTION_HINT, true);

    private final InteractiveOptionBroker<CodeEditorChangesOption> exitOnChanges =
            new InteractiveOptionBroker<>(
                    "exit-on-changes",
                    "Unsaved Changes",
                    "You are about to close the editor for {0} and you have unsaved changes.\nPlease choose whether to save or discard the changes." + REMEMBER_OPTION_HINT,
                    CodeEditorChangesOption.ASK,
                    CodeEditorChangesOption.SAVE,
                    CodeEditorChangesOption.DISCARD,
                    CodeEditorChangesOption.SHOW,
                    CodeEditorChangesOption.CANCEL);


    public CodeEditorConfirmationSettings(CodeEditorSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.codeEditor.title.ConfirmationSettings");
    }

    @Override
    public String getHelpTopic() {
        return "codeEditorConfirmationSettings";
    }


    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public CodeEditorConfirmationSettingsForm createConfigurationEditor() {
        return new CodeEditorConfirmationSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "confirmations";
    }

    @Override
    public void readConfiguration(Element element) {
        saveChanges.readConfiguration(element);
        revertChanges.readConfiguration(element);
        exitOnChanges.readConfiguration(element);
    }

    @Override
    public void writeConfiguration(Element element) {
        saveChanges.writeConfiguration(element);
        revertChanges.writeConfiguration(element);
        exitOnChanges.writeConfiguration(element);
    }
}
