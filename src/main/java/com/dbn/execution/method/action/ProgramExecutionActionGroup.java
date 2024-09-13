package com.dbn.execution.method.action;

import com.dbn.editor.DBContentType;
import com.dbn.object.DBMethod;
import com.dbn.object.DBProgram;
import com.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ProgramExecutionActionGroup extends DefaultActionGroup {

    public ProgramExecutionActionGroup(DBSchemaObject object) {
        super("Execute", true);
        if (object.getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
            add(new ProgramMethodRunAction((DBProgram) object));
            add(new ProgramMethodDebugAction((DBProgram) object));
        } else {
            add(new MethodRunAction((DBMethod) object, false));
            add(new MethodDebugAction((DBMethod) object, false));
        }
    }
}