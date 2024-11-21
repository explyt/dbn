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

package com.dbn.editor.code.action;

import com.dbn.common.icon.Icons;
import com.dbn.editor.code.SourceCodeEditor;
import com.dbn.editor.code.diff.SourceCodeDiffManager;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.dispose.Checks.isValid;

public class CompareWithDatabaseAction extends AbstractCodeEditorDiffAction {

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull SourceCodeEditor fileEditor,
            @NotNull DBSourceCodeVirtualFile sourceCodeFile) {

    SourceCodeDiffManager diffManager = SourceCodeDiffManager.getInstance(project);
        diffManager.opedDatabaseDiffWindow(sourceCodeFile);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable SourceCodeEditor fileEditor, @Nullable DBSourceCodeVirtualFile sourceCodeFile) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Compare with Database");
        presentation.setEnabled(isValid(fileEditor) && isValid(sourceCodeFile));
        presentation.setIcon(Icons.CODE_EDITOR_DIFF_DB);
    }
}
