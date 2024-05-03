package com.dbn.execution.common.message.ui.tree.node;

import com.dbn.common.ui.tree.TreeEventType;
import com.dbn.common.ui.tree.Trees;
import com.dbn.connection.ConnectionId;
import com.dbn.execution.common.message.ui.tree.MessagesTreeBundleNode;
import com.dbn.execution.compiler.CompilerMessage;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.lookup.DBObjectRef;
import com.dbn.vfs.file.DBEditableObjectVirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.List;

public class CompilerMessagesObjectNode extends MessagesTreeBundleNode<CompilerMessagesNode, CompilerMessageNode> {
    private final DBObjectRef<DBSchemaObject> object;

    CompilerMessagesObjectNode(CompilerMessagesNode parent, DBObjectRef<DBSchemaObject> object) {
        super(parent);
        this.object = object;
    }

    @Override
    @Nullable
    public DBEditableObjectVirtualFile getFile() {
        DBSchemaObject schemaObject = getObject();
        if (schemaObject != null) {
            return schemaObject.getEditableVirtualFile();
        }
        return null;
    }

    @Nullable
    @Override
    public ConnectionId getConnectionId() {
        return object.getConnectionId();
    }

    @Nullable
    public DBSchemaObject getObject() {
        return DBObjectRef.get(object);
    }

    public DBObjectRef<DBSchemaObject> getObjectRef() {
        return object;
    }

    TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        List<CompilerMessageNode> children = getChildren();

        children.removeIf(n -> isOverwrite(n.getMessage(), compilerMessage));
        CompilerMessageNode messageNode = new CompilerMessageNode(this, compilerMessage);
        addChild(messageNode);

        getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        return Trees.createTreePath(messageNode);
    }

    private static boolean isOverwrite(CompilerMessage oldMessage, CompilerMessage newMessage) {
        // if message is not part of the same result and target is the same, the old message can be overwritten
        return !oldMessage.isSameResult(newMessage) && oldMessage.isSameTarget(newMessage);
    }

    @Nullable
    public TreePath getTreePath(CompilerMessage compilerMessage) {
        for (CompilerMessageNode messageNode : getChildren()) {
            if (messageNode.getMessage() == compilerMessage) {
                return Trees.createTreePath(messageNode);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return object.toString();
    }
}
