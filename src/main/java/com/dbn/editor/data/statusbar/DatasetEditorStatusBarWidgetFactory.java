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

package com.dbn.editor.data.statusbar;

import com.dbn.common.dispose.Disposer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.component.Components.projectService;

public class DatasetEditorStatusBarWidgetFactory implements StatusBarWidgetFactory {
    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DatasetEditorStatusBarWidgetFactory";
    }

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "DB Table Math";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return !project.isDefault();
    }

    @NotNull
    @Override
    public StatusBarWidget createWidget(@NotNull Project project) {
        return projectService(project, DatasetEditorStatusBarWidget.class);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
