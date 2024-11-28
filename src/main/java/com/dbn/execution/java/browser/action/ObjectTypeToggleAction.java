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
 *
 */

package com.dbn.execution.java.browser.action;

import com.dbn.common.action.ToggleAction;
import com.dbn.execution.java.browser.ui.JavaExecutionBrowserForm;
import com.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ObjectTypeToggleAction extends ToggleAction {
    private JavaExecutionBrowserForm browserComponent;
    private final DBObjectType objectType;

    public ObjectTypeToggleAction(JavaExecutionBrowserForm browserComponent, DBObjectType objectType) {
        super("Show " + objectType.getListName(), null, objectType.getIcon());
        this.objectType = objectType;
        this.browserComponent = browserComponent;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return browserComponent.getSettings().getObjectVisibility(objectType);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        browserComponent.setObjectsVisible(objectType, state);
    }
}
