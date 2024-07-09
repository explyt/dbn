package com.dbn.common.ui.tree;

public class EmptyTreeModel implements ReadonlyTreeModel{
    @Override
    public Object getRoot() {
        return null;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return true;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return -1;
    }
}
