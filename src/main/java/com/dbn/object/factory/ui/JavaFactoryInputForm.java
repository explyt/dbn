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

package com.dbn.object.factory.ui;

import com.dbn.common.color.Colors;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.component.DBNComponent;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.object.DBSchema;
import com.dbn.object.factory.JavaFactoryInput;
import com.dbn.object.factory.ObjectFactoryInput;
import com.dbn.object.factory.ui.common.ObjectFactoryInputForm;
import com.dbn.object.lookup.DBObjectRef;
import com.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Icon;
import javax.swing.JComboBox;
import java.awt.BorderLayout;
import java.awt.Color;

import static com.dbn.common.ui.util.TextFields.onTextChange;
import static com.dbn.common.util.Strings.toUpperCase;
import static com.dbn.generator.code.java.JavaCodeGeneratorInput.isValidClassName;
import static com.dbn.generator.code.java.JavaCodeGeneratorInput.isValidPackageName;

public class JavaFactoryInputForm extends ObjectFactoryInputForm<JavaFactoryInput> {
    private JPanel mainPanel;
    private JLabel connectionLabel;
    private JLabel schemaLabel;
    protected JTextField classNameTextField;
    private JPanel headerPanel;
    private JLabel nameLabel;
    private JTextField packageTextField;
    private JComboBox<String> javaType;

    private final DBObjectRef<DBSchema> schema;

    public JavaFactoryInputForm(DBNComponent parent, DBSchema schema, DBObjectType objectType, int index) {
        super(parent, schema.getConnection(), objectType, index);
        this.schema = DBObjectRef.of(schema);
        connectionLabel.setText(getConnection().getName());
        connectionLabel.setIcon(getConnection().getIcon());

        schemaLabel.setText(schema.getName());
        schemaLabel.setIcon(schema.getIcon());

        DBNHeaderForm headerForm = createHeaderForm(schema, objectType);
        onTextChange(packageTextField, e -> headerForm.setTitle(getSchema().getName() +  "." + toUpperCase(packageTextField.getText()) + (classNameTextField.getText().isEmpty() ? "" : "." + toUpperCase(classNameTextField.getText()))));
        onTextChange(classNameTextField, e -> headerForm.setTitle(getSchema().getName() + (packageTextField.getText().isEmpty() ? "" : "." + toUpperCase(packageTextField.getText()))  + "." + toUpperCase(classNameTextField.getText())));

        javaType.addActionListener(e -> {
            String selectedItem = (String) javaType.getSelectedItem();
            nameLabel.setText("Java " + selectedItem + " name");
        });

        javaType.setRenderer((list, value, index1, isSelected, cellHasFocus) -> {
            JLabel label = new JLabel();
            if (value != null) {
                label.setText(value);
                switch (value){
                    case "Class":
                        label.setIcon(Icons.DBO_JAVA_CLASS);
                        break;
                    case "Interface":
                        label.setIcon(Icons.DBO_JAVA_INTERFACE);
                        break;
                    case "Enum":
                        label.setIcon(Icons.DBO_JAVA_ENUMERATION);
                        break;
                    case "Annotation":
                        label.setIcon(Icons.DBO_JAVA_ANNOTATION);
                        break;
                    case "Exception":
                        label.setIcon(Icons.DBO_JAVA_EXCEPTION);
                }
            }
            return label;
        });
    }

    private DBNHeaderForm createHeaderForm(DBSchema schema, DBObjectType objectType) {
        String headerTitle = schema.getName() + ".[unnamed]";
        Icon headerIcon = objectType.getIcon();
        Color headerBackground = Colors.getPanelBackground();
        if (getEnvironmentSettings(schema.getProject()).getVisibilitySettings().getDialogHeaders().value()) {
            headerBackground = schema.getEnvironmentType().getColor();
        }
        DBNHeaderForm headerForm = new DBNHeaderForm(
                this, headerTitle,
                headerIcon,
                headerBackground
        );
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        return headerForm;
    }

    @Override
    public JavaFactoryInput createFactoryInput(ObjectFactoryInput parent) {
        return new JavaFactoryInput(getSchema(), packageTextField.getText(), classNameTextField.getText(), (String) javaType.getSelectedItem(), getObjectType(), getIndex());
    }

    @Override
    protected void initValidation() {
        formValidator.addTextValidation(packageTextField, p -> isValidPackageName(p), "Invalid package name");
        formValidator.addTextValidation(classNameTextField, p -> isValidClassName(p), "Invalid class name");
    }

    DBSchema getSchema() {
        return DBObjectRef.get(schema);
    }

    @Override
    public void focus() {
        classNameTextField.requestFocus();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
