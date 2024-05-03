package com.dbn.editor.data.action;

import com.dbn.common.action.ContextAction;
import com.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDataEditorAction extends ContextAction<DatasetEditor> {

    @Override
    protected DatasetEditor getTarget(@NotNull AnActionEvent e) {
        return DatasetEditor.get(e);
    }
}
