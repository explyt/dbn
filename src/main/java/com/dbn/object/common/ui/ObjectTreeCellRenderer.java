package com.dbn.object.common.ui;

import com.dbn.common.ui.tree.DBNColoredTreeCellRenderer;
import com.dbn.common.ui.tree.DBNTree;
import com.dbn.common.ui.tree.Trees;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBMethod;
import com.dbn.object.common.DBObject;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;

public class ObjectTreeCellRenderer extends DBNColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull DBNTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof DBObjectRef) {
            DBObjectRef<?> objectRef = (DBObjectRef) userObject;
            append(objectRef.getObjectName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

            DBObject object = DBObjectRef.get(objectRef);
            setIcon(object == null ? objectRef.getObjectType().getIcon() : object.getOriginalIcon());

            if (object instanceof DBMethod || object instanceof DBJavaMethod) {
                if (object.getOverload() > 0) {
                    append(" #" + object.getOverload(), SimpleTextAttributes.GRAY_ATTRIBUTES);
                }
            }

        } else {
            append(userObject.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
        Trees.applySpeedSearchHighlighting(tree, this, true, selected);
    }
}
