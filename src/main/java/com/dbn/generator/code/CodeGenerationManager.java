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

package com.dbn.generator.code;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.common.options.setting.Settings;
import com.dbn.common.util.Dialogs;
import com.dbn.connection.context.DatabaseContext;
import com.dbn.generator.code.java.impl.JdbcConnectorCodeGenerator;
import com.dbn.generator.code.shared.CodeGenerator;
import com.dbn.generator.code.shared.ui.CodeGeneratorInputDialog;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dbn.common.component.Components.projectService;
import static com.dbn.editor.DatabaseEditorStateManager.COMPONENT_NAME;

@State(
        name = COMPONENT_NAME,
        storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
@Getter
@Setter
public class CodeGenerationManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.CodeGenerationManager";
    private static final Map<CodeGeneratorType, CodeGenerator> CODE_GENERATORS = new HashMap<>();
    static {
        new JdbcConnectorCodeGenerator(CodeGeneratorType.DATABASE_CONNECTOR);
        new JdbcConnectorCodeGenerator(CodeGeneratorType.DATABASE_CONNECTOR_SID);
        new JdbcConnectorCodeGenerator(CodeGeneratorType.DATABASE_CONNECTOR_SERVICE_NAME);
        new JdbcConnectorCodeGenerator(CodeGeneratorType.DATABASE_CONNECTOR_TNS);
        //...
    }

    private String targetModuleSelection;

    private CodeGenerationManager(Project project) {
        super(project, COMPONENT_NAME);
    }

    public static CodeGenerationManager getInstance(@NotNull Project project) {
        return projectService(project, CodeGenerationManager.class);
    }

    public static void registerCodeGenerator(CodeGenerator codeGenerator) {
        CODE_GENERATORS.put(codeGenerator.getType(), codeGenerator);
    }

    public void openCodeGeneratorDialog(CodeGeneratorType type, DatabaseContext context) {
        CodeGenerator codeGenerator = CODE_GENERATORS.get(type);
        Dialogs.show(() -> new CodeGeneratorInputDialog(context, codeGenerator));
    }

    public static List<CodeGenerator> getCodeGenerators() {
        return new ArrayList<>(CODE_GENERATORS.values());
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        Settings.setString(element, "target-module-selection", targetModuleSelection);
        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        targetModuleSelection = Settings.getString(element, "target-module-selection", targetModuleSelection);
    }

}
