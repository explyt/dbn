package com.dbn.data.find.action;

import com.dbn.data.find.DataSearchComponent;
import com.intellij.find.FindSettings;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class WholeWordsToggleAction extends DataSearchHeaderToggleAction {
    public WholeWordsToggleAction(DataSearchComponent searchComponent) {
        super(
                searchComponent,
                "W&hole Words",
                AllIcons.Actions.Words,
                AllIcons.Actions.WordsHovered,
                AllIcons.Actions.WordsSelected);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return getFindModel().isWholeWordsOnly();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabled(!getFindModel().isRegularExpressions());
        e.getPresentation().setVisible(true);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        FindSettings.getInstance().setLocalWholeWordsOnly(state);
        getFindModel().setWholeWordsOnly(state);
    }
}
