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

package com.dbn.object.filter.custom.ui.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.object.filter.custom.ObjectFilter;
import com.dbn.object.filter.custom.ui.ObjectFilterExpressionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

import static com.dbn.nls.NlsResources.txt;

public class EditObjectFilterAction extends BasicAction {
    private final ObjectFilterExpressionForm form;

    public EditObjectFilterAction(ObjectFilterExpressionForm form) {
        this.form = form;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.ACTION_EDIT);
        presentation.setText(txt("app.objects.action.EditFilter"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ObjectFilter<?> filter = form.getFilter();
        form.getParentForm().showFilterDetailsDialog(filter, false, () -> form.setExpression(filter.getExpression()));
    }

}
