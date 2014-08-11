package com.dci.intellij.dbn.data.grid.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridSettingsForm;
import com.intellij.openapi.project.Project;

public class DataGridSettings extends CompositeProjectConfiguration<DataGridSettingsForm> {
    private DataGridTrackingColumnSettings trackingColumnSettings;
    private DataGridSortingSettings sortingSettings;

    public DataGridSettings(Project project) {
        super(project);
        trackingColumnSettings = new DataGridTrackingColumnSettings(project);
        sortingSettings = new DataGridSortingSettings(project);
    }

    public static DataGridSettings getInstance(Project project) {
        return getGlobalProjectSettings(project).getDataGridSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DataGridSettings";
    }

    public String getDisplayName() {
        return "Data Grid";
    }

    public String getHelpTopic() {
        return "dataGrid";
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/


    public DataGridTrackingColumnSettings getTrackingColumnSettings() {
       return trackingColumnSettings;
    }

    public DataGridSortingSettings getSortingSettings() {
        return sortingSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public DataGridSettingsForm createConfigurationEditor() {
        return new DataGridSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "dataset-grid-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                trackingColumnSettings,
                sortingSettings};
    }
}
