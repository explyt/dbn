package com.dci.intellij.dbn.debugger.jdwp.config;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfig;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;

public class DBMethodJdwpRunConfig extends DBMethodRunConfig<DBMethodJdwpRunConfigEditor> {

    public DBMethodJdwpRunConfig(Project project, ConfigurationFactory factory, String name, boolean generic) {
        super(project, factory, name, generic);
    }

    @Override
    protected DBMethodJdwpRunConfigEditor createConfigurationEditor() {
        return new DBMethodJdwpRunConfigEditor(this);
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new DBMethodJdwpRunProfileState(env);
    }

    public String createSuggestedName() {
        MethodExecutionInput executionInput = getExecutionInput();
        if (executionInput == null) {
            return "<unnamed>";
        } else {
            return executionInput.getMethodRef().getObjectName();
        }
    }

}
