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

package com.dbn.execution.statement.action;

import com.dbn.common.action.BackgroundUpdate;
import com.dbn.common.action.Lookups;
import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.shortcut.OverridingShortcutInterceptor;
import com.dbn.common.util.Editors;
import com.dbn.debugger.DatabaseDebuggerManager;
import com.dbn.execution.statement.StatementExecutionManager;
import com.dbn.language.common.DBLanguagePsiFile;
import com.dbn.language.common.psi.PsiUtil;
import com.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.dispose.Checks.isNotValid;
import static com.dbn.nls.NlsResources.txt;

@BackgroundUpdate
public class ExecuteStatementEditorAction extends ProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookups.getEditor(e);
        if (isNotValid(editor)) return;

        FileEditor fileEditor = Editors.getFileEditor(editor);
        if (isNotValid(fileEditor)) return;

        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
        executionManager.executeStatementAtCursor(fileEditor);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(isEnabled(e));
        presentation.setIcon(Icons.STMT_EXECUTION_RUN);
        presentation.setText(txt("app.execution.action.ExecuteStatement"));
        presentation.setVisible(isVisible(e));
    }

    private static boolean isEnabled(AnActionEvent e) {
        Project project = Lookups.getProject(e);
        if (isNotValid(project)) return false;

        Editor editor = Lookups.getEditor(e);
        if (isNotValid(editor)) return false;

        PsiFile psiFile = PsiUtil.getPsiFile(project, editor.getDocument());
        if (!(psiFile instanceof DBLanguagePsiFile)) return false;

        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile instanceof DBSourceCodeVirtualFile) return false;

        return true;
    }

    public static boolean isVisible(AnActionEvent e) {
        VirtualFile virtualFile = Lookups.getVirtualFile(e);
        return !DatabaseDebuggerManager.isDebugConsole(virtualFile);
    }

    /**
     * Ctrl-S override
     */
    public static class ShortcutInterceptor extends OverridingShortcutInterceptor {
        public ShortcutInterceptor() {
            super("DBNavigator.Actions.Execute");
        }

        @Override
        protected boolean canDelegateExecute(AnActionEvent e) {
            return isEnabled(e);
        }
    }
}
