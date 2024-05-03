package com.dbn.browser.options;

import com.dbn.browser.options.ui.DatabaseBrowserSettingsForm;
import com.dbn.common.options.CompositeProjectConfiguration;
import com.dbn.common.options.Configuration;
import com.dbn.options.ConfigId;
import com.dbn.options.ProjectSettings;
import com.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = false)
public class DatabaseBrowserSettings
        extends CompositeProjectConfiguration<ProjectSettings, DatabaseBrowserSettingsForm>
        implements TopLevelConfig {

    private final @Getter(lazy = true) DatabaseBrowserGeneralSettings generalSettings = new DatabaseBrowserGeneralSettings(this);
    private final @Getter(lazy = true) DatabaseBrowserFilterSettings filterSettings   = new DatabaseBrowserFilterSettings(this);
    private final @Getter(lazy = true) DatabaseBrowserSortingSettings sortingSettings = new DatabaseBrowserSortingSettings(this);
    private final @Getter(lazy = true) DatabaseBrowserEditorSettings editorSettings   = new DatabaseBrowserEditorSettings(this);

    public DatabaseBrowserSettings(ProjectSettings parent) {
        super(parent);
    }

    @NotNull
    @Override
    public DatabaseBrowserSettingsForm createConfigurationEditor() {
        return new DatabaseBrowserSettingsForm(this);
    }

    public static DatabaseBrowserSettings getInstance(@NotNull Project project) {
        return ProjectSettings.get(project).getBrowserSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DatabaseBrowserSettings";
    }

    @Override
    public String getDisplayName() {
        return "Database Browser";
    }

    @Override
    public String getHelpTopic() {
        return "browserSettings";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.BROWSER;
    }

    @NotNull
    @Override
    public DatabaseBrowserSettings getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                getGeneralSettings(),
                getFilterSettings(),
                getSortingSettings(),
                getEditorSettings()};
    }

    @Override
    public String getConfigElementName() {
        return "browser-settings";
    }
}
