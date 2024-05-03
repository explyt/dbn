package com.dbn.editor.data.state.column.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.list.CheckBoxList;
import com.dbn.editor.data.state.column.ui.ColumnStateSelectable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class RevertColumnOrderAction extends BasicAction {
    private final CheckBoxList list;

    public RevertColumnOrderAction(CheckBoxList list)  {
        super("Revert Column Order", null, Icons.ACTION_REVERT);
        this.list = list;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        list.sortElements(ColumnStateSelectable.POSITION_COMPARATOR);
    }
}
