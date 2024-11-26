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

package com.dbn.generator.code.java.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.common.ref.WeakRef;
import com.dbn.connection.context.DatabaseContext;
import com.dbn.generator.code.CodeGenerationManager;
import com.dbn.generator.code.CodeGeneratorType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaCodeGenerationAction extends ProjectAction {
    private final WeakRef<DatabaseContext> context;
    private final CodeGeneratorType type;

    public JavaCodeGenerationAction(DatabaseContext context, CodeGeneratorType type) {
        super(type.getName(), null, null);
        this.context = WeakRef.of(context);
        this.type = type;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        CodeGenerationManager manager = CodeGenerationManager.getInstance(project);
        manager.openCodeGenerator(type, getContext());
    }

    @Nullable
    private DatabaseContext getContext() {
        return WeakRef.get(context);
    }
}
