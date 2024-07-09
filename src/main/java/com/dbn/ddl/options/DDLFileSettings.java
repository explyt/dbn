package com.dbn.ddl.options;

import com.dbn.common.options.CompositeProjectConfiguration;
import com.dbn.common.options.Configuration;
import com.dbn.ddl.options.ui.DDFileSettingsForm;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettings;
import com.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = false)
public class DDLFileSettings extends CompositeProjectConfiguration<ProjectSettings, DDFileSettingsForm> implements TopLevelConfig {
    private final @Getter(lazy = true) DDLFileExtensionSettings extensionSettings = new DDLFileExtensionSettings(this);
    private final @Getter(lazy = true) DDLFileGeneralSettings generalSettings = new DDLFileGeneralSettings(this);

    public DDLFileSettings(ProjectSettings parent) {
        super(parent);
    }

    public static DDLFileSettings getInstance(@NotNull Project project) {
        return ProjectSettings.get(project).getDdlFileSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DDLFileSettings";
    }

    @Override
    public String getDisplayName() {
        return txt("cfg.ddlFiles.title.DdlFiles");
    }

    @Override
    public String getHelpTopic() {
        return "ddlFileSettings";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.DDL_FILES;
    }

    @NotNull
    @Override
    public DDLFileSettings getOriginalSettings() {
        return getInstance(getProject());
    }

    /********************************************************
    *                     Configuration                     *
    *********************************************************/
    @Override
    @NotNull
    public DDFileSettingsForm createConfigurationEditor() {
        return new DDFileSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "ddl-file-settings";
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                getExtensionSettings(),
                getGeneralSettings()};
    }
}
