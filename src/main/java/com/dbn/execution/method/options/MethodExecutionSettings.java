package com.dbn.execution.method.options;

import com.dbn.common.options.BasicProjectConfiguration;
import com.dbn.common.options.setting.Settings;
import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.common.project.ProjectSupplier;
import com.dbn.execution.common.options.ExecutionEngineSettings;
import com.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dbn.execution.method.options.ui.MethodExecutionSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class MethodExecutionSettings extends BasicProjectConfiguration<ExecutionEngineSettings, ConfigurationEditorForm> implements ExecutionTimeoutSettings, ProjectSupplier {
    private int executionTimeout = 30;
    private int debugExecutionTimeout = 600;
    private int parameterHistorySize = 10;

    public MethodExecutionSettings(ExecutionEngineSettings parent) {
        super(parent);
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.execution.title.MethodExecution");
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
    public ConfigurationEditorForm createConfigurationEditor() {
        return new MethodExecutionSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "method-execution";
    }

    @Override
    public void readConfiguration(Element element) {
        executionTimeout = Settings.getInteger(element, "execution-timeout", executionTimeout);
        debugExecutionTimeout = Settings.getInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        parameterHistorySize = Settings.getInteger(element, "parameter-history-size", parameterHistorySize);

    }

    @Override
    public void writeConfiguration(Element element) {
        Settings.setInteger(element, "execution-timeout", executionTimeout);
        Settings.setInteger(element, "debug-execution-timeout", debugExecutionTimeout);
        Settings.setInteger(element, "parameter-history-size", parameterHistorySize);
    }
}
