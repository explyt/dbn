package com.dbn.code.common.style.options.ui;

import com.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dbn.common.icon.Icons;
import com.dbn.common.options.Configuration;
import com.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dbn.common.ui.tab.DBNTabbedPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CodeStyleSettingsForm extends CompositeConfigurationEditorForm<ProjectCodeStyleSettings> {
    private JPanel mainPanel;
    private final DBNTabbedPane languageTabs;

    public CodeStyleSettingsForm(ProjectCodeStyleSettings settings) {
        super(settings);
        languageTabs = new DBNTabbedPane(this);
        //languageTabs.setAdjustBorders(false);
        mainPanel.add(languageTabs, BorderLayout.CENTER);
        addSettingsPanel(settings.getSQLCodeStyleSettings(), Icons.FILE_SQL);
        addSettingsPanel(settings.getPSQLCodeStyleSettings(), Icons.FILE_PLSQL);
    }

    private void addSettingsPanel(Configuration configuration, Icon icon) {
        JComponent component = configuration.createComponent();
        languageTabs.addTab(configuration.getDisplayName(), icon, component);
    }


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
