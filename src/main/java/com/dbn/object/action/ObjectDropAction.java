package com.dbn.object.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.icon.Icons;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.factory.DatabaseObjectFactory;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ObjectDropAction extends BasicAction {
    private DBObjectRef<DBSchemaObject> object;

    public ObjectDropAction(DBSchemaObject object) {
        super("Drop...", null, Icons.ACTION_CLOSE);
        this.object = DBObjectRef.of(object);
    }

    public DBSchemaObject getObject() {
        return DBObjectRef.ensure(object);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DBSchemaObject object = getObject();
        DatabaseObjectFactory objectFactory = DatabaseObjectFactory.getInstance(object.getProject());
        objectFactory.dropObject(object);
    }
}