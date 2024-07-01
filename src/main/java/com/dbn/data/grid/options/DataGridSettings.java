package com.dbn.data.grid.options;

import com.dbn.common.options.CompositeProjectConfiguration;
import com.dbn.common.options.Configuration;
import com.dbn.data.grid.options.ui.DataGridSettingsForm;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettings;
import com.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = false)
public class DataGridSettings extends CompositeProjectConfiguration<ProjectSettings, DataGridSettingsForm> implements TopLevelConfig {
    private final @Getter(lazy = true) DataGridGeneralSettings generalSettings = new DataGridGeneralSettings(this);
    private final @Getter(lazy = true) DataGridSortingSettings sortingSettings = new DataGridSortingSettings(this);
    private final @Getter(lazy = true) DataGridAuditColumnSettings auditColumnSettings = new DataGridAuditColumnSettings(this);

    public DataGridSettings(ProjectSettings parent) {
        super(parent);
    }

    public static DataGridSettings getInstance(@NotNull Project project) {
        return ProjectSettings.get(project).getDataGridSettings();
    }

    public static boolean isAuditColumn(Project project, String name) {
        DataGridSettings dataGridSettings = getInstance(project);
        return dataGridSettings.getAuditColumnSettings().isAuditColumn(name);
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DataGridSettings";
    }

    @Override
    public String getDisplayName() {
        return nls("cfg.data.title.DataGrids");
    }

    @Override
    public String getHelpTopic() {
        return "dataGrid";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.DATA_GRID;
    }

    @NotNull
    @Override
    public DataGridSettings getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    @Override
    @NotNull
    public DataGridSettingsForm createConfigurationEditor() {
        return new DataGridSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "dataset-grid-settings";
    }

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                getGeneralSettings(),
                getSortingSettings(),
                getAuditColumnSettings()
        };
    }
}
