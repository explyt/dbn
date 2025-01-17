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

package com.dbn.object.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.common.icon.Icons;
import com.dbn.editor.DatabaseFileEditorManager;
import com.dbn.editor.EditorProviderId;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.nls.NlsResources.txt;

public class ObjectEditCodeAction extends ProjectAction {
    private final DBObjectRef<DBSchemaObject> object;

    ObjectEditCodeAction(DBSchemaObject object) {
        this.object = DBObjectRef.of(object);
        setDefaultIcon(true);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText(txt("app.objects.action.EditCode"));
        presentation.setIcon(Icons.OBJECT_EDIT_SOURCE);
    }

    @Nullable
    @Override
    public  Project getProject() {
        return getObject().getProject();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DBSchemaObject schemaObject = getObject();
        DatabaseFileEditorManager editorManager = DatabaseFileEditorManager.getInstance(project);
        editorManager.connectAndOpenEditor(schemaObject, EditorProviderId.CODE, false, true);
    }

    @NotNull
    private DBSchemaObject getObject() {
        return DBObjectRef.ensure(object);
    }
}
