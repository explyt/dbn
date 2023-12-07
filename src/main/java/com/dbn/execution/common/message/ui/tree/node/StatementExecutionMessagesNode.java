package com.dbn.execution.common.message.ui.tree.node;

import com.dbn.common.ui.tree.TreeEventType;
import com.dbn.execution.common.message.ui.tree.MessagesTreeBundleNode;
import com.dbn.execution.common.message.ui.tree.MessagesTreeRootNode;
import com.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.Objects;

public class StatementExecutionMessagesNode extends MessagesTreeBundleNode<MessagesTreeRootNode, StatementExecutionMessagesFileNode> {
    public StatementExecutionMessagesNode(MessagesTreeRootNode parent) {
        super(parent);
    }

    @Nullable
    private StatementExecutionMessagesFileNode getChildTreeNode(VirtualFile file) {
        for (StatementExecutionMessagesFileNode messagesTreeNode : getChildren()) {
            if (Objects.equals(messagesTreeNode.getFile(), file)) {
                return messagesTreeNode;
            }
        }
        return null;
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        StatementExecutionMessagesFileNode node = getFileTreeNode(executionMessage);
        if (node == null) {
            node = new StatementExecutionMessagesFileNode(this, executionMessage.getVirtualFile());
            addChild(node);
            getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return node.addExecutionMessage(executionMessage);
    }


    @Nullable
    public TreePath getTreePath(StatementExecutionMessage statementExecutionMessage) {
        StatementExecutionMessagesFileNode messagesFileNode = getFileTreeNode(statementExecutionMessage);
        if (messagesFileNode != null) {
            return messagesFileNode.getTreePath(statementExecutionMessage);
        }
        return null;
    }

    @Nullable
    private StatementExecutionMessagesFileNode getFileTreeNode(StatementExecutionMessage statementExecutionMessage) {
        return getChildTreeNode(statementExecutionMessage.getVirtualFile());
    }
}
