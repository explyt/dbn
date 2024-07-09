package com.dbn.object.dependency.ui;

import com.dbn.common.latent.Latent;
import com.dbn.common.ui.SpeedSearchBase;
import com.dbn.common.util.Commons;
import com.dbn.object.common.DBObject;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class ObjectDependencyTreeSpeedSearch extends SpeedSearchBase<ObjectDependencyTree> implements Disposable, TreeModelListener {

    private final Latent<Object[]> elements = Latent.basic(() -> loadElements());

    public ObjectDependencyTreeSpeedSearch(ObjectDependencyTree tree) {
        super(tree);
    }

    @Override
    protected int getSelectedIndex() {
        Object[] elements = getAllElements();
        ObjectDependencyTreeNode treeNode = getSelectedTreeElement();
        if (treeNode == null) return -1;

        for (int i=0; i<elements.length; i++) {
            if (treeNode == elements[i]) {
                return i;
            }
        }
        return -1;
    }

    private ObjectDependencyTreeNode getSelectedTreeElement() {
        TreePath selectionPath = getComponent().getSelectionPath();
        if (selectionPath == null) return null;

        return (ObjectDependencyTreeNode) selectionPath.getLastPathComponent();
    }

    @Override
    protected Object[] getElements() {
        return elements.get();
    }

    @NotNull
    private Object[] loadElements() {
        List<ObjectDependencyTreeNode> nodes = new ArrayList<>();
        ObjectDependencyTreeNode root = getComponent().getModel().getRoot();
        loadElements(nodes, root);
        return nodes.toArray();
    }

    private static void loadElements(List<ObjectDependencyTreeNode> nodes, ObjectDependencyTreeNode browserTreeNode) {
        nodes.add(browserTreeNode);
        List<ObjectDependencyTreeNode> children = browserTreeNode.getChildren(false);
        if (children != null) {
            for (ObjectDependencyTreeNode child : children) {
                loadElements(nodes, child);
            }

        }
    }

    @Override
    @Nullable
    protected String getElementText(Object o) {
        ObjectDependencyTreeNode treeNode = (ObjectDependencyTreeNode) o;
        DBObject object = treeNode.getObject();

        ObjectDependencyTreeNode rootNode = treeNode.getModel().getRoot();
        DBObject rootObject = rootNode.getObject();
        if (rootObject != null && object != null) {
            if (Commons.match(rootObject.getSchema(), object.getSchema())) {
                return object.getName();
            } else {
                return object.getSchemaName() + "." + object.getName();
            }
        }

        return null;
    }

    @Override
    protected void selectElement(Object o, String s) {
        ObjectDependencyTreeNode treeNode = (ObjectDependencyTreeNode) o;
        getComponent().selectElement(treeNode);
    }


    @Override
    public void treeNodesChanged(TreeModelEvent e) { elements.reset(); }

    @Override
    public void treeNodesInserted(TreeModelEvent e) { elements.reset(); }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) { elements.reset(); }

    @Override
    public void treeStructureChanged(TreeModelEvent e) { elements.reset(); }


    @Override
    public void dispose() {
    }
}
