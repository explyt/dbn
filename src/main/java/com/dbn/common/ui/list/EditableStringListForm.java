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

package com.dbn.common.ui.list;

import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.util.UserInterface;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.NotNull;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

public class EditableStringListForm extends DBNFormBase {
    private JPanel component;
    private JLabel titleLabel;
    private JPanel listPanel;

    private final EditableStringList editableStringList;

    public EditableStringListForm(DBNComponent parent, String title, boolean sorted) {
        this(parent, title, new ArrayList<>(), sorted);
    }

    public EditableStringListForm(DBNComponent parent, String title, List<String> elements, boolean sorted) {
        super(parent);
        editableStringList = new EditableStringList(this, elements, sorted, false);
        ToolbarDecorator decorator = UserInterface.createToolbarDecorator(editableStringList);
        decorator.setAddAction(anActionButton -> editableStringList.insertRow());
        decorator.setRemoveAction(anActionButton -> editableStringList.removeRow());
        decorator.setMoveUpAction(anActionButton -> editableStringList.moveRowUp());
        decorator.setMoveDownAction(anActionButton -> editableStringList.moveRowDown());
        titleLabel.setText(title);
        //decorator.setPreferredSize(new Dimension(200, 300));
        JPanel editableListPanel = decorator.createPanel();
        Container parentContainer = editableStringList.getParent();
        parentContainer.setBackground(editableStringList.getBackground());
        this.listPanel.add(editableListPanel, BorderLayout.CENTER);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return component;
    }

    public List<String> getStringValues() {
        return editableStringList.getStringValues();
    }

    public void setStringValues(List<String> stringValues) {
        editableStringList.setStringValues(stringValues);
    }
}
