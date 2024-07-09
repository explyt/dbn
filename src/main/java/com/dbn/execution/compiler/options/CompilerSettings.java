package com.dbn.execution.compiler.options;

import com.dbn.common.options.BasicConfiguration;
import com.dbn.common.options.setting.Settings;
import com.dbn.connection.operation.options.OperationSettings;
import com.dbn.execution.compiler.CompileDependenciesOption;
import com.dbn.execution.compiler.CompileType;
import com.dbn.execution.compiler.options.ui.CompilerSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class CompilerSettings extends BasicConfiguration<OperationSettings, CompilerSettingsForm> {
    private CompileType compileType = CompileType.KEEP;
    private CompileDependenciesOption compileDependenciesOption = CompileDependenciesOption.ASK;
    private boolean alwaysShowCompilerControls = false;

    public CompilerSettings(OperationSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.compiler.title.CompilerSettings");
    }

    @Override
    public String getHelpTopic() {
        return "executionEngine";
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
    @Override
    @NotNull
    public CompilerSettingsForm createConfigurationEditor() {
        return new CompilerSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "compiler";
    }

    @Override
    public void readConfiguration(Element element) {
        compileType = Settings.getEnum(element, "compile-type", compileType);
        compileDependenciesOption = Settings.getEnum(element, "compile-dependencies", compileDependenciesOption);
        alwaysShowCompilerControls = Settings.getBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }

    @Override
    public void writeConfiguration(Element element) {
        Settings.setEnum(element, "compile-type", compileType);
        Settings.setEnum(element, "compile-dependencies", compileDependenciesOption);
        Settings.setBoolean(element, "always-show-controls", alwaysShowCompilerControls);
    }
}
