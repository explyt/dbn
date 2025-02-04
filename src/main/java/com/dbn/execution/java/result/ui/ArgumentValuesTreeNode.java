/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.execution.java.result.ui;

import com.dbn.execution.common.input.ExecutionValue;
import com.dbn.object.common.DBObject;
import com.dbn.object.lookup.DBObjectRef;
import lombok.Getter;
import lombok.Setter;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class ArgumentValuesTreeNode implements TreeNode{
    private final DBObjectRef object;
    private Object userValue;
    private final ArgumentValuesTreeNode parent;
    private final List<ArgumentValuesTreeNode> children = new ArrayList<>();

    protected ArgumentValuesTreeNode(ArgumentValuesTreeNode parent, DBObjectRef object, Object userValue) {
        this.parent = parent;
        this.object = object;
        if (parent != null) {
            parent.children.add(this);
        }
        this.userValue = userValue;
    }

	public void dispose() {
        for (ArgumentValuesTreeNode treeNode : children) {
            treeNode.dispose();
        }
    }

    public ArgumentValuesTreeNode initChild(DBObject object) {
        for (ArgumentValuesTreeNode treeNode : children) {
            if (Objects.equals(object.ref(), treeNode.getObject())) {
                return treeNode;
            }
        }
        return new ArgumentValuesTreeNode(this, object.ref(), null);
    }

    @Override
    public String toString() {
        if (userValue instanceof ExecutionValue) {
            ExecutionValue fieldValue = (ExecutionValue) userValue;
            return String.valueOf(fieldValue.getValue());
        }

        if (userValue instanceof DBObject) {
            DBObject object = (DBObject) userValue;
            return object.getName();
        }

        return userValue.toString();
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    @Override
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return children.size();
    }

    @Override
    public TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public boolean isLeaf() {
        return children.size() == 0;
    }

    @Override
    public Enumeration children() {
        return Collections.enumeration(children);
    }
}
