package com.dbn.common.ui.tree;

import com.dbn.common.ui.util.Listeners;
import com.dbn.common.thread.Dispatch;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

@Slf4j
@UtilityClass
public final class Trees {
    public static void applySpeedSearchHighlighting(
            @NotNull JComponent tree,
            @NotNull SimpleColoredComponent coloredComponent,
            boolean mainTextOnly,
            boolean selected) {
        try {
            SpeedSearchUtil.applySpeedSearchHighlighting(tree, coloredComponent, true, selected);
        } catch (Throwable e) {
            conditionallyLog(e);
            log.warn("Error applying speed search highlighting");
        }
    }

    public static TreePath createTreePath(TreeNode treeNode) {
        List<TreeNode> list = new ArrayList<>();
        list.add(treeNode);
        TreeNode parent = treeNode.getParent();
        while (parent != null) {
            list.add(0, parent);
            parent = parent.getParent();
        }
        return new TreePath(list.toArray());
    }

    public static void collapseAll(JTree tree) {
        int row = tree.getRowCount() - 1;
        while (row >= 0) {
            tree.collapseRow(row);
            row--;
        }
    }

    public static void expandAll(JTree tree) {
        Object root = tree.getModel().getRoot();
        if (root != null) {
            tree.expandPath(new TreePath(root));
            int oldRowCount = 0;
            do {
                int rowCount = tree.getRowCount();
                if (rowCount == oldRowCount) break;
                oldRowCount = rowCount;
                for (int i = 0; i < rowCount; i++) {
                    tree.expandRow(i);
                }
            }
            while (true);
        }
    }

    public static void attachStickyPath(DBNTree tree, @Nullable Supplier<Boolean> enabled) {
        new DBNStickyPathTree(tree) {
            @Override
            protected boolean checkFeatureEnabled() {
                return enabled == null || enabled.get();
            }
        };
    }

    public static void notifyTreeModelListeners(Object source, Listeners<TreeModelListener> listeners, @Nullable TreePath path, TreeEventType eventType) {
        if (path == null) return;

        TreeModelEvent event = new TreeModelEvent(source, path);
        notifyTreeModelListeners(listeners, eventType, event);
    }

    private static void notifyTreeModelListeners(Listeners<TreeModelListener> listeners, final TreeEventType eventType, final TreeModelEvent event) {
        Dispatch.run(() -> {
            try {
                Object lastPathComponent = event.getTreePath().getLastPathComponent();
                if (lastPathComponent == null) return;

                listeners.notify(l -> {
                    switch (eventType) {
                        case NODES_ADDED:       l.treeNodesInserted(event);    break;
                        case NODES_REMOVED:     l.treeNodesRemoved(event);     break;
                        case NODES_CHANGED:     l.treeNodesChanged(event);     break;
                        case STRUCTURE_CHANGED: l.treeStructureChanged(event); break;
                    }
                });
            } catch (IndexOutOfBoundsException e) {
                conditionallyLog(e);
                // tree may have mutated already
            }
        });
    }

    public static TreePath getPathAtMousePosition(JTree tree) {
        Point location = MouseInfo.getPointerInfo().getLocation();
        return getPathForLocation(tree, location);
    }

    public static TreePath getPathAtMousePosition(JTree tree, MouseEvent event) {
        return getPathForLocation(tree, event.getPoint());
    }

    public static TreePath getPathForLocation(JTree tree, Point location) {
        return tree.getClosestPathForLocation((int) location.getX(), (int) location.getY());
    }
}
