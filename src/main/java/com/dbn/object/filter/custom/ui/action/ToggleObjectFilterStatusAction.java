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
import com.dbn.object.filter.custom.ui.ObjectFilterExpressionForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

import static com.dbn.nls.NlsResources.txt;

public class ToggleObjectFilterStatusAction extends BasicAction {
    private ObjectFilterExpressionForm form;

    public ToggleObjectFilterStatusAction(ObjectFilterExpressionForm form) {
        this.form = form;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(
                form.isActive() ?
                        Icons.COMMON_FILTER_ACTIVE :
                        Icons.COMMON_FILTER_INACTIVE);
        presentation.setText(
                form.isActive() ?
                        txt("app.objects.action.DeactivateFilter") :
                        txt("app.objects.action.ActivateFilter"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        form.setActive(!form.isActive());
    }
}
