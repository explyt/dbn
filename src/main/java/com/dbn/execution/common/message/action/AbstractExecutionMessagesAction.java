package com.dbn.execution.common.message.action;

import com.dbn.common.action.ContextAction;
import com.dbn.common.ref.WeakRef;
import com.dbn.execution.common.message.ui.tree.MessagesTree;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract class AbstractExecutionMessagesAction extends ContextAction<MessagesTree> {
    private final WeakRef<MessagesTree> messagesTree;

    AbstractExecutionMessagesAction(MessagesTree messagesTree) {
        this.messagesTree = WeakRef.of(messagesTree);
    }

    @Nullable
    protected MessagesTree getTarget(@NotNull AnActionEvent e) {
        return WeakRef.get(messagesTree);
    }
}
