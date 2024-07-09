package com.dbn.code.common.style.options;

import com.dbn.code.common.style.options.ui.CodeStyleCaseSettingsForm;
import com.dbn.common.options.BasicConfiguration;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.dbn.common.options.setting.Settings.*;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public abstract class CodeStyleCaseSettings extends BasicConfiguration<DBLCodeStyleSettings, CodeStyleCaseSettingsForm> {
    private final Map<String, CodeStyleCaseOption> options = new LinkedHashMap<>();
    private boolean enabled = true;

    public CodeStyleCaseSettings(DBLCodeStyleSettings parent) {
        super(parent);
        addOption("KEYWORD_CASE", CodeStyleCase.LOWER, false);
        addOption("FUNCTION_CASE", CodeStyleCase.LOWER, false);
        addOption("PARAMETER_CASE", CodeStyleCase.LOWER, false);
        addOption("DATATYPE_CASE", CodeStyleCase.LOWER, false);
        addOption("OBJECT_CASE", CodeStyleCase.PRESERVE, true);
    }

    private void addOption(String id, CodeStyleCase option, boolean ignoreMixedCase) {
        options.put(id, new CodeStyleCaseOption(id, option, ignoreMixedCase));
    }


    @Override
    public String getDisplayName() {
        return txt("cfg.codeStyle.title.CaseOptions");
    }

    public CodeStyleCaseOption getKeywordCaseOption() {
        return getCodeStyleCaseOption("KEYWORD_CASE");
    }

    public CodeStyleCaseOption getFunctionCaseOption() {
        return getCodeStyleCaseOption("FUNCTION_CASE");
    }

    public CodeStyleCaseOption getParameterCaseOption() {
        return getCodeStyleCaseOption("PARAMETER_CASE");
    }

    public CodeStyleCaseOption getDatatypeCaseOption() {
        return getCodeStyleCaseOption("DATATYPE_CASE");
    }


    public CodeStyleCaseOption getObjectCaseOption() {
        return getCodeStyleCaseOption("OBJECT_CASE");
    }

    private CodeStyleCaseOption getCodeStyleCaseOption(String name) {
        return options.get(name);
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    @NotNull
    public CodeStyleCaseSettingsForm createConfigurationEditor() {
        return new CodeStyleCaseSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "case-options";
    }

    @Override
    public void readConfiguration(Element element) {
        enabled = booleanAttribute(element, "enabled", enabled);
        for (Element child : element.getChildren()) {
            String name = stringAttribute(child, "name");
            CodeStyleCaseOption option = getCodeStyleCaseOption(name);
            if (option != null) {
                option.readConfiguration(child);
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        setBooleanAttribute(element, "enabled", enabled);
        for (CodeStyleCaseOption option : options.values()) {
            Element optionElement = newElement(element,"option");
            option.writeConfiguration(optionElement);
        }
    }
}
