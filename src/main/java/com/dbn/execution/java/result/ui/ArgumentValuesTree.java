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

import com.dbn.common.color.Colors;
import com.dbn.common.ui.tree.DBNColoredTreeCellRenderer;
import com.dbn.common.ui.tree.DBNTree;
import com.dbn.common.util.TextAttributes;
import com.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dbn.execution.common.input.ExecutionValue;
import com.dbn.object.DBJavaField;
import com.dbn.object.DBJavaMethod;
import com.dbn.object.DBJavaParameter;
import com.dbn.object.common.DBObject;
import com.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;

import static com.dbn.object.lookup.DBJavaNameCache.getCanonicalName;
import static com.intellij.ui.SimpleTextAttributes.GRAY_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.REGULAR_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;

class ArgumentValuesTree extends DBNTree{

    ArgumentValuesTree(JavaExecutionResultForm parent, List<ExecutionValue> inputValues) {
        super(parent, createModel(parent, inputValues));
        setCellRenderer(new CellRenderer());
        Color bgColor = TextAttributes.getSimpleTextAttributes(DataGridTextAttributesKeys.PLAIN_DATA).getBgColor();
        setBackground(bgColor == null ? Colors.getTableBackground() : bgColor);

        addTreeSelectionListener(createTreeSelectionListener());
    }


    @NotNull
    public JavaExecutionResultForm getParentForm() {
        return this.getParentComponent();
    }

    @NotNull
    private static ArgumentValuesTreeModel createModel(JavaExecutionResultForm parentForm, List<ExecutionValue> inputValues) {
        return new ArgumentValuesTreeModel(parentForm.getMethod(), inputValues);
    }

    private TreeSelectionListener createTreeSelectionListener() {
        return e -> {
            TreePath path = e.getPath();
            ArgumentValuesTreeNode treeNode = (ArgumentValuesTreeNode) path.getLastPathComponent();
            if (treeNode == null) return;

            Object userValue = treeNode.getUserValue();
            if (userValue instanceof ExecutionValue) {
                ExecutionValue fieldValue = (ExecutionValue) userValue;
                DBJavaParameter argument = null; // TODO inputValue.getArgument();
                if (argument == null) return;

                Object value = fieldValue.getValue();
                if (value instanceof ResultSet || fieldValue.isLargeObject() || fieldValue.isLargeValue()) {
                    getParentForm().selectArgumentOutputTab(argument);
                }
            }
        };
    }

    static class CellRenderer extends DBNColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull DBNTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            ArgumentValuesTreeNode treeNode = (ArgumentValuesTreeNode) value;
            Object userValue = treeNode.getUserValue();
            DBObject object = DBObjectRef.get(treeNode.getObject());

            if (object instanceof DBJavaMethod) {
                DBJavaMethod method = (DBJavaMethod) object;
                setIcon(method.getIcon());
                append(method.getSignature(), REGULAR_ATTRIBUTES);
                return;
            }

            if (object != null) {
                setIcon(object.getIcon());
                append(object.getName(), REGULAR_ATTRIBUTES);
            }

            if (userValue instanceof String) {
                append((String) userValue, treeNode.isLeaf() ?
                        REGULAR_ATTRIBUTES :
                        REGULAR_BOLD_ATTRIBUTES);
            }

            if (userValue instanceof ExecutionValue) {
                ExecutionValue fieldValue = (ExecutionValue) userValue;
                String stringValue = Objects.toString(fieldValue.getValue());
                append(" = ", REGULAR_ATTRIBUTES);
                append(stringValue, REGULAR_BOLD_ATTRIBUTES);
            }

            if (object instanceof DBJavaParameter) {
                DBJavaParameter parameter = (DBJavaParameter) object;
                String dataType = parameter.isClass() ?
                        getCanonicalName(parameter.getJavaClassName()) :
                        parameter.getBaseType();

                append(" (" + dataType + ")", GRAY_ATTRIBUTES);
            } else

            if (object instanceof DBJavaField) {
                DBJavaField field = (DBJavaField) object;
                String dataType = field.isClass() ?
                        getCanonicalName(field.getJavaClassName()) :
                        field.getBaseType();

                append(" (" + dataType + ")", GRAY_ATTRIBUTES);
            }

        }
    }
}
