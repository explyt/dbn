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

package com.dbn.generator.code.java.impl;

import com.dbn.common.dispose.Failsafe;
import com.dbn.common.outcome.MessageOutcomeHandler;
import com.dbn.common.outcome.OutcomeType;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.context.DatabaseContext;
import com.dbn.generator.code.CodeGeneratorType;
import com.dbn.generator.code.java.JavaCodeGenerator;
import com.dbn.generator.code.shared.ui.CodeGeneratorInputDialog;
import com.dbn.generator.code.shared.ui.CodeGeneratorInputForm;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;

public class JdbcConnectorCodeGenerator extends JavaCodeGenerator<JdbcConnectorCodeGeneratorInput, JdbcConnectorCodeGeneratorResult> {
    public JdbcConnectorCodeGenerator(CodeGeneratorType type) {
        super(type);
    }

    @Override
    public boolean supports(DatabaseContext context) {
        return context instanceof ConnectionHandler;
    }

    @Override
    public JdbcConnectorCodeGeneratorInput createInput(DatabaseContext context) {
        Project project = context.getProject();
        JdbcConnectorCodeGeneratorInput input = new JdbcConnectorCodeGeneratorInput(context);
        input.addOutcomeHandler(OutcomeType.FAILURE, MessageOutcomeHandler.get(project));
        return input;
    }

    @Override
    public CodeGeneratorInputForm<JdbcConnectorCodeGeneratorInput> createInputForm(CodeGeneratorInputDialog dialog, JdbcConnectorCodeGeneratorInput input) {
        return null;
    }

    @Override
    public JdbcConnectorCodeGeneratorResult performCodeGeneration(JdbcConnectorCodeGeneratorInput input, DatabaseContext context) throws Exception {
        String templateName = getType().getTemplate();

        Project project = Failsafe.nd(context.getProject());
        FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
        FileTemplate template = templateManager.getTemplate(templateName);

        PsiDirectory directory = input.getTargetDirectory();
        PsiElement javaClass = FileTemplateUtil.createFromTemplate(template, null, null, directory);
        VirtualFile javaFile = javaClass.getContainingFile().getVirtualFile();

        JdbcConnectorCodeGeneratorResult result = new JdbcConnectorCodeGeneratorResult(input);
        result.addGeneratedFile(javaFile);
        return result;
    }

    @Override
    protected String getTitle(OutcomeType outcomeType) {
        switch (outcomeType) {
            case SUCCESS: return "Success";
            case FAILURE: return "Failure";
        }
        return "";
    }

    @Override
    protected String getMessage(OutcomeType outcomeType) {
        switch (outcomeType) {
            case SUCCESS: return "Successfully created Jdbc Connector";
            case FAILURE: return "Failed to create Jdbc Connector";
        }
        return "";
    }
}
