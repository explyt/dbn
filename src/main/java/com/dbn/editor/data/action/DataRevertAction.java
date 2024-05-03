package com.dbn.editor.data.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.data.model.DatasetEditorModelCell;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class DataRevertAction extends BasicAction {
    private final DatasetEditorModelCell cell;

    public DataRevertAction(DatasetEditorModelCell cell) {
        super("Revert Changes", null, Icons.ACTION_REVERT);
        this.cell = cell;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        cell.revertChanges();
    }


}
