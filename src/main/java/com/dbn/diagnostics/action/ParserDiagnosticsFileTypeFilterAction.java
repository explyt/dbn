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

import com.dbn.common.action.BasicAction;
import com.dbn.common.action.ComboBoxAction;
import com.dbn.common.util.Strings;
import com.dbn.diagnostics.ParserDiagnosticsManager;
import com.dbn.diagnostics.data.ParserDiagnosticsFilter;
import com.dbn.diagnostics.ui.ParserDiagnosticsForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

import static com.dbn.nls.NlsResources.txt;

public class ParserDiagnosticsFileTypeFilterAction extends ComboBoxAction implements DumbAware {
    private final ParserDiagnosticsForm form;

    public ParserDiagnosticsFileTypeFilterAction(ParserDiagnosticsForm form) {
        this.form = form;
    }

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(@NotNull JComponent component, @NotNull DataContext dataContext) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new SelectFilterValueAction(null));
        actionGroup.addSeparator();
        ParserDiagnosticsManager manager = form.getManager();
        String[] fileExtensions = manager.getFileExtensions();
        for (String fileType : fileExtensions) {
            actionGroup.add(new SelectFilterValueAction(fileType));
        }
        return actionGroup;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();

        ParserDiagnosticsFilter resultFilter = getResultFilter();
        String fileType = resultFilter.getFileType();
        presentation.setText(Strings.isEmpty(fileType) ? txt("app.diagnostics.action.FileTypeFilter") : "*." + fileType, false);
    }

    private ParserDiagnosticsFilter getResultFilter() {
        return form.getManager().getResultFilter();
    }

    private class SelectFilterValueAction extends BasicAction {
        private final String fileType;

        public SelectFilterValueAction(String fileType) {
            super(Strings.isEmpty(fileType) ? txt("app.shared.action.NoFilter") : "*." + fileType);
            this.fileType = fileType;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            getResultFilter().setFileType(fileType);
            form.refreshResult();
        }
    }
 }