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

package com.dbn.generator.code.java.ui;

import com.dbn.common.project.ModulePresentable;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.connection.context.DatabaseContext;
import com.dbn.generator.code.CodeGenerationManager;
import com.dbn.generator.code.java.JavaCodeGeneratorInput;
import com.dbn.generator.code.shared.ui.CodeGeneratorInputDialog;
import com.dbn.generator.code.shared.ui.CodeGeneratorInputForm;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.util.List;

import static com.dbn.common.ui.util.ComboBoxes.initComboBox;
import static com.dbn.common.ui.util.ComboBoxes.selectElement;
import static com.dbn.common.ui.util.ComboBoxes.selectFirstElement;

public class JavaCodeGeneratorInputForm<I extends JavaCodeGeneratorInput> extends CodeGeneratorInputForm<I> {
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JPanel targetLocationPanel;
    private JComboBox<ModulePresentable> moduleComboBox;
    private JTextField textField1;

    public JavaCodeGeneratorInputForm(CodeGeneratorInputDialog dialog, I input) {
        super(dialog, input);

        DatabaseContext databaseContext = input.getDatabaseContext();
        DBNHeaderForm headerForm = new DBNHeaderForm(this, databaseContext);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        initModules();
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }



    private void initModules() {
        Project project = ensureProject();
        ModuleManager moduleManager = ModuleManager.getInstance(project);
        Module[] modules = moduleManager.getSortedModules();

        CodeGenerationManager codeGenerationManager = CodeGenerationManager.getInstance(project);

        List<ModulePresentable> presentableModules = ModulePresentable.fromModules(modules);
        initComboBox(moduleComboBox, presentableModules);
        moduleComboBox.addItemListener(e -> {
            if (e.getStateChange() != ItemEvent.SELECTED) return;
            ModulePresentable presentable = (ModulePresentable) e.getItem();
            codeGenerationManager.setTargetModuleSelection(presentable.getName());
        });

        String targetModuleSelection = codeGenerationManager.getTargetModuleSelection();
        if (targetModuleSelection == null)
            selectFirstElement(moduleComboBox); else
            selectElement(moduleComboBox, targetModuleSelection);

    }




}
