package com.dci.intellij.dbn.code.common.completion.options.filter.ui;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFilterSettings;
import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFiltersSettings;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CodeCompletionFiltersSettingsForm extends CompositeConfigurationEditorForm<CodeCompletionFiltersSettings> {

    private JLabel basicCompletionLabel;
    private JLabel extendedCompletionLabel;
    private JPanel mainPanel;
    private JScrollPane basicScrollPane;
    private JScrollPane extendedScrollPane;

    public CodeCompletionFiltersSettingsForm(CodeCompletionFiltersSettings filtersSettings) {
        super(filtersSettings);
        CodeCompletionFilterSettings basicFilterSettings = filtersSettings.getBasicFilterSettings();
        CodeCompletionFilterSettings extendedFilterSettings = filtersSettings.getExtendedFilterSettings();

        basicScrollPane.setViewportView(basicFilterSettings.createComponent());
        extendedScrollPane.setViewportView(extendedFilterSettings.createComponent());

        Shortcut[] basicShortcuts = KeyUtil.getShortcuts(IdeActions.ACTION_CODE_COMPLETION);
        Shortcut[] extendedShortcuts = KeyUtil.getShortcuts(IdeActions.ACTION_SMART_TYPE_COMPLETION);

        basicCompletionLabel.setText("Basic (" + KeymapUtil.getShortcutsText(basicShortcuts) + ')');
        extendedCompletionLabel.setText("Extended (" + KeymapUtil.getShortcutsText(extendedShortcuts) + ')');
        updateBorderTitleForeground(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
