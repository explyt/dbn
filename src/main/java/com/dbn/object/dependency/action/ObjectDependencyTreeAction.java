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

package com.dbn.object.dependency.action;

import com.dbn.object.action.AnObjectAction;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.dependency.ObjectDependencyManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.nls.NlsResources.txt;

public class ObjectDependencyTreeAction extends AnObjectAction<DBSchemaObject> {
    public ObjectDependencyTreeAction(DBSchemaObject schemaObject) {
        super(schemaObject);
        setDefaultIcon(true);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DBSchemaObject object) {

        ObjectDependencyManager dependencyManager = ObjectDependencyManager.getInstance(project);
        dependencyManager.openDependencyTree(object);
    }

    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable DBSchemaObject target) {
        presentation.setText(txt("app.objects.action.DependencyTree"));
    }
}