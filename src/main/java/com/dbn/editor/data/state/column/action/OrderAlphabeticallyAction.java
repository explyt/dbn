package com.dbn.editor.data.state.column.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.list.CheckBoxList;
import com.dbn.editor.data.state.column.ui.ColumnStateSelectable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class OrderAlphabeticallyAction extends BasicAction {
    private final CheckBoxList list;

    public OrderAlphabeticallyAction(CheckBoxList list)  {
        super("Order Columns Alphabetically", null, Icons.ACTION_SORT_ALPHA);
        this.list = list;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        list.sortElements(ColumnStateSelectable.NAME_COMPARATOR);
    }
}
