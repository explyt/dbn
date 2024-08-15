package com.dbn.object.dependency.ui;

import com.dbn.common.icon.CompositeIcon;
import com.dbn.common.load.LoadInProgressIcon;
import com.dbn.common.ui.tree.DBNColoredTreeCellRenderer;
import com.dbn.common.ui.tree.DBNTree;
import com.dbn.common.ui.tree.Trees;
import com.dbn.common.util.Commons;
import com.dbn.object.common.DBObject;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ObjectDependencyTreeCellRenderer extends DBNColoredTreeCellRenderer {

    public static final JBColor HIGHLIGHT_BACKGROUND = new JBColor(0xCCCCFF, 0x155221);

    @Override
    public void customizeCellRenderer(@NotNull DBNTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        ObjectDependencyTreeNode node = (ObjectDependencyTreeNode) value;
        DBObject object = node.getObject();

        if (object != null) {
            ObjectDependencyTreeNode selectedNode = (ObjectDependencyTreeNode) tree.getLastSelectedPathComponent();
            boolean isLoading = node.isLoading();
            boolean highlight = !isLoading && selectedNode != null && selectedNode != node && Commons.match(object, selectedNode.getObject());

            SimpleTextAttributes regularAttributes = highlight ?
                    SimpleTextAttributes.REGULAR_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_PLAIN, null, HIGHLIGHT_BACKGROUND, null) :
                    isLoading ? SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES : SimpleTextAttributes.REGULAR_ATTRIBUTES;
            SimpleTextAttributes grayAttributes = highlight ?
                    SimpleTextAttributes.GRAY_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_PLAIN, null, new JBColor(0xCCCCFF, 0x155221), null) :
                    isLoading ? SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES : SimpleTextAttributes.GRAY_ATTRIBUTES;

            Icon objectIcon = object.getIcon();
            ObjectDependencyTreeModel model = node.getModel();
            Icon dependencyTypeIcon = model.getDependencyType().getSoftIcon();
            Icon icon = node.getParent() == null ? objectIcon :
                    objectIcon == null ? dependencyTypeIcon : new CompositeIcon(dependencyTypeIcon, objectIcon, 1);
            setIcon(icon);

            setBackground(selected ? UIUtil.getTreeSelectionBackground(true) : regularAttributes.getBgColor());
            //if (highlight) setBorder(new LineBorder(JBColor.red)); else setBorder(null);
            boolean appendSchema = true;

            ObjectDependencyTreeNode rootNode = model.getRoot();
            DBObject rootObject = rootNode.getObject();
            if (rootObject == null || Commons.match(rootObject.getSchema(), object.getSchema())) {
                appendSchema = false;
            }

            append(object.getName(), regularAttributes);
            if (appendSchema) {
                append(" (" + object.getSchema().getName() + ")", grayAttributes);
            }

            Trees.applySpeedSearchHighlighting(tree, this, true, selected);
        } else {
            setIcon(LoadInProgressIcon.INSTANCE);
            append("Loading...", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
        }
    }

    @Override
    protected boolean shouldDrawBackground() {
        return isFocused();
    }
}
