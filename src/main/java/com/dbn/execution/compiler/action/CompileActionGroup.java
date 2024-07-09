package com.dbn.execution.compiler.action;

import com.dbn.common.icon.Icons;
import com.dbn.database.DatabaseFeature;
import com.dbn.editor.DBContentType;
import com.dbn.execution.compiler.CompileType;
import com.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;

public class CompileActionGroup extends DefaultActionGroup implements DumbAware {

    public CompileActionGroup(DBSchemaObject object) {
        super("Compile", true);
        boolean debugSupported = DatabaseFeature.DEBUGGING.isSupported(object);
        getTemplatePresentation().setIcon(Icons.OBJECT_COMPILE);
        if (object.getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
            add(new CompileObjectAction(object, DBContentType.CODE_SPEC_AND_BODY, CompileType.NORMAL));
            if (debugSupported) {
                add(new CompileObjectAction(object, DBContentType.CODE_SPEC_AND_BODY, CompileType.DEBUG));
            }
        } else {
            add(new CompileObjectAction(object, DBContentType.CODE, CompileType.NORMAL));
            if (debugSupported) {
                add(new CompileObjectAction(object, DBContentType.CODE, CompileType.DEBUG));
            }
        }

        addSeparator();
        add(new CompileInvalidObjectsAction(object.getSchema()));
    }
}