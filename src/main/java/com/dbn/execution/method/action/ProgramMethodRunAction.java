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

package com.dbn.execution.method.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.method.MethodExecutionManager;
import com.dbn.execution.method.ui.MethodExecutionHistory;
import com.dbn.object.DBMethod;
import com.dbn.object.DBProgram;
import com.dbn.object.action.ObjectListShowAction;
import com.dbn.object.common.DBObject;
import com.intellij.openapi.actionSystem.AnAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.dbn.common.util.Unsafe.cast;
import static com.dbn.nls.NlsResources.txt;

public class ProgramMethodRunAction extends ObjectListShowAction {
    public ProgramMethodRunAction(DBProgram program) {
        super(txt("app.execution.action.Run"), program);
        getTemplatePresentation().setIcon(Icons.METHOD_EXECUTION_RUN);
    }

    @Nullable
    @Override
    public List<DBObject> getRecentObjectList() {
        DBProgram program = (DBProgram) getSourceObject();
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(program.getProject());
        MethodExecutionHistory executionHistory = methodExecutionManager.getExecutionHistory();
        return cast(executionHistory.getRecentlyExecutedMethods(program));
    }


    @Override
    public List<DBObject> getObjectList() {
        DBProgram program = (DBProgram) getSourceObject();
        List<DBObject> objects = new ArrayList<>();
        objects.addAll(program.getProcedures());
        objects.addAll(program.getFunctions());
        return objects;
    }

    @Override
    public String getTitle() {
        return "Select method to execute";
    }

    @Override
    public String getEmptyListMessage() {
        DBProgram program = (DBProgram) getSourceObject();
        return "The " + program.getQualifiedNameWithType() + " has no methods to execute.";
    }


    @Override
    public String getListName() {
       return "executable elements";
   }

    @Override
    protected AnAction createObjectAction(DBObject object) {
        return new MethodRunAction((DBMethod) object, true);
    }
}