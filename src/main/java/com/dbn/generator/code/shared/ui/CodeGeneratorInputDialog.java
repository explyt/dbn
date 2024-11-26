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

package com.dbn.generator.code.shared.ui;

import com.dbn.common.outcome.DialogCloseOutcomeHandler;
import com.dbn.common.outcome.MessageOutcomeHandler;
import com.dbn.common.outcome.OutcomeHandler;
import com.dbn.common.outcome.OutcomeType;
import com.dbn.common.ref.WeakRef;
import com.dbn.common.ui.dialog.DBNDialog;
import com.dbn.connection.context.DatabaseContext;
import com.dbn.generator.code.CodeGenerationManager;
import com.dbn.generator.code.shared.CodeGenerator;
import com.dbn.generator.code.shared.CodeGeneratorInput;
import com.dbn.generator.code.shared.CodeGeneratorResult;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;
import java.util.List;

import static com.dbn.common.util.Editors.openFileEditor;

@Getter
public class CodeGeneratorInputDialog extends DBNDialog<CodeGeneratorInputForm> {
    private final WeakRef<DatabaseContext>databaseContext;
    private final CodeGenerator codeGenerator;

    public CodeGeneratorInputDialog(DatabaseContext databaseContext, CodeGenerator codeGenerator) {
        super(databaseContext.getProject(), "Generate Code (" + codeGenerator.getType().getName() + ")", false);
        this.databaseContext = WeakRef.of(databaseContext);
        this.codeGenerator = codeGenerator;

        init();
    }

    @NotNull
    @Override
    protected CodeGeneratorInputForm createForm() {
        CodeGeneratorInput input = createInput();
        return codeGenerator.createInputForm(this, input);
    }

    private @NotNull CodeGeneratorInput createInput() {
        Project project = getProject();
        DatabaseContext databaseContext = getDatabaseContext();
        CodeGeneratorInput input = codeGenerator.createInput(databaseContext);
        input.addOutcomeHandler(OutcomeType.FAILURE, MessageOutcomeHandler.get(project));
        input.addOutcomeHandler(OutcomeType.SUCCESS, DialogCloseOutcomeHandler.create(this));
        input.addOutcomeHandler(OutcomeType.SUCCESS, createFilesOpener(project));
        return input;
    }

    private void generateCode() {
        Project project = getProject();
        CodeGenerationManager codeGenerationManager = CodeGenerationManager.getInstance(project);
        codeGenerationManager.generateCode(codeGenerator, getInput());
    }

    @NotNull
    public DatabaseContext getDatabaseContext() {
        return WeakRef.ensure(databaseContext);
    }

    @NotNull
    private static OutcomeHandler createFilesOpener(Project project) {
        return (OutcomeHandler.LowPriority) outcome -> {
            CodeGeneratorResult<?> data = outcome.getData();

            List<VirtualFile> generatedFiles = data.getGeneratedFiles();
            for (VirtualFile generatedFile : generatedFiles) {
                openFileEditor(project, generatedFile, true);
            }
        };
    }

    @NotNull
    @Override
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction()};
    }

    @Override
    protected void doOKAction() {
        generateCode();
    }

    private CodeGeneratorInput getInput() {
        return getForm().getInput();
    }
}
