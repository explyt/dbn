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

package com.dbn.code.common.intention;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Context;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.mapping.FileConnectionContextManager;
import com.dbn.language.common.DBLanguagePsiFile;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dbn.assistant.editor.AssistantPrompt.Flavor.COMMENT;
import static com.dbn.common.util.Editors.isMainEditor;
import static com.dbn.common.util.Files.isDbLanguagePsiFile;
import static com.dbn.connection.ConnectionHandler.isLiveConnection;
import static com.dbn.nls.NlsResources.txt;

public class SelectSchemaIntentionAction extends EditorIntentionAction  {
    @Override
    public EditorIntentionType getType() {
        return EditorIntentionType.SELECT_SCHEMA;
    }

    @Override
    @NotNull
    public String getText() {
        return txt("app.codeEditor.action.SetCurrentSchema");
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.FILE_SCHEMA_MAPPING;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        if (isDatabaseAssistantPrompt(editor, psiElement, COMMENT)) return false;

        PsiFile psiFile = psiElement.getContainingFile();
        if (!isDbLanguagePsiFile(psiFile)) return false;
        if (!isMainEditor(editor)) return false;

        VirtualFile file = psiFile.getVirtualFile();
        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        if (!contextManager.isSchemaSelectable(file)) return false;


        DBLanguagePsiFile dbFile = (DBLanguagePsiFile) psiFile;
        ConnectionHandler connection = dbFile.getConnection();
        return isLiveConnection(connection);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        PsiFile psiFile = psiElement.getContainingFile();
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguageFile = (DBLanguagePsiFile) psiFile;
            FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
            DataContext dataContext = Context.getDataContext(editor);
            contextManager.promptSchemaSelector(dbLanguageFile.getVirtualFile(), dataContext, null);
        }
    }
}
