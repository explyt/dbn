package com.dbn.common.ui.tree;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public interface ReadonlyTreeModel extends TreeModel {
    @Override
    default void addTreeModelListener(TreeModelListener l) {

    }

    @Override
    default void removeTreeModelListener(TreeModelListener l) {

    }

    @Override
    default void valueForPathChanged(TreePath path, Object newValue) {

    }
}
