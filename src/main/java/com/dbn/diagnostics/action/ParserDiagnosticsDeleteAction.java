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

package com.dbn.diagnostics.action;

import com.dbn.common.icon.Icons;
import com.dbn.common.util.Messages;
import com.dbn.diagnostics.ParserDiagnosticsManager;
import com.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.util.Conditional.when;
import static com.dbn.nls.NlsResources.txt;

public class ParserDiagnosticsDeleteAction extends AbstractParserDiagnosticsAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ParserDiagnosticsForm form) {
        ParserDiagnosticsResult result = form.getSelectedResult();
        if (result != null) {
            Messages.showQuestionDialog(project,
                    txt("msg.diagnostics.title.DeleteResult"),
                    txt("msg.diagnostics.message.DeleteResultConfirmation",result.getName()),
                    Messages.OPTIONS_YES_NO, 0,
                    option -> when(option == 0, () -> {
                        ParserDiagnosticsManager manager = getManager(project);
                        manager.deleteResult(result);
                        ParserDiagnosticsResult latestResult = manager.getLatestResult();
                        form.refreshResults();
                        form.selectResult(latestResult);
                    }));
        }


    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ParserDiagnosticsForm form) {
        presentation.setText(txt("app.diagnostics.action.DeleteResult"));
        presentation.setIcon(Icons.ACTION_DELETE);
        if (form != null) {
            ParserDiagnosticsResult result = form.getSelectedResult();
            presentation.setEnabled(result != null);
        } else {
            presentation.setEnabled(false);
        }

    }
}
