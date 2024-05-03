package com.dbn.code.common.completion.options.sorting.action;

import com.dbn.code.common.completion.options.sorting.CodeCompletionSortingSettings;
import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.ListUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class MoveUpAction extends BasicAction {
    private final CodeCompletionSortingSettings settings;
    private final JList list;

    public MoveUpAction(JList list, CodeCompletionSortingSettings settings)  {
        super("Move Up", null, Icons.ACTION_MOVE_UP);
        this.list = list;
        this.settings = settings;
    }

    @Override
    public void update(AnActionEvent e) {
        int[] indices = list.getSelectedIndices();
        boolean enabled =
                list.isEnabled() &&
                indices.length > 0 &&
                indices[0] > 0;
        e.getPresentation().setEnabled(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ListUtil.moveSelectedItemsUp(list);
        settings.setModified(true);
    }
}
