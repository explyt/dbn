package com.dbn.common.ui.tree;

import javax.swing.event.TreeExpansionEvent;

public interface TreeExpansionListener extends javax.swing.event.TreeExpansionListener {
    @Override
    default void treeExpanded(TreeExpansionEvent event) {

    }

    @Override
    default void treeCollapsed(TreeExpansionEvent event) {

    }
}
