package com.dbn.code.common.style.options;

import com.dbn.code.common.style.options.ui.CodeStyleSettingsForm;
import com.dbn.code.psql.style.PSQLCodeStyle;
import com.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dbn.code.sql.style.SQLCodeStyle;
import com.dbn.code.sql.style.options.SQLCodeStyleSettings;
import com.dbn.common.options.CompositeProjectConfiguration;
import com.dbn.common.options.Configuration;
import com.dbn.options.ProjectSettings;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ProjectCodeStyleSettings extends CompositeProjectConfiguration<ProjectSettings, CodeStyleSettingsForm> {
    public ProjectCodeStyleSettings(ProjectSettings parent){
        super(parent);
    }

    public static ProjectCodeStyleSettings getInstance(@NotNull Project project) {
        return ProjectSettings.get(project).getCodeStyleSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.CodeStyleSettings";
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.codeStyle.title.CodeStyle");
    }

    @Override
    @NotNull
    public CodeStyleSettingsForm createConfigurationEditor() {
        return new CodeStyleSettingsForm(this);
    }

    public SQLCodeStyleSettings getSQLCodeStyleSettings() {
        return SQLCodeStyle.settings(getProject());
    }

    public PSQLCodeStyleSettings getPSQLCodeStyleSettings() {
        return PSQLCodeStyle.settings(getProject());
    }

    /*********************************************************
    *                     Configuration                      *
    *********************************************************/
    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                getSQLCodeStyleSettings(),
                getPSQLCodeStyleSettings()};
    }
}
