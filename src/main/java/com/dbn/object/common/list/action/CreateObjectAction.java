package com.dbn.object.common.list.action;

import com.dbn.common.action.BasicAction;
import com.dbn.common.dispose.Failsafe;
import com.dbn.common.ref.WeakRef;
import com.dbn.object.DBSchema;
import com.dbn.object.common.list.DBObjectList;
import com.dbn.object.factory.DatabaseObjectFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CreateObjectAction extends BasicAction {

    private final WeakRef<DBObjectList> objectList;

    CreateObjectAction(DBObjectList objectList) {
        super("New " + objectList.getObjectType().getName() + "...");
        this.objectList = WeakRef.of(objectList);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DBObjectList objectList = getObjectList();
        DBSchema schema = objectList.ensureParentEntity();
        Project project = schema.getProject();
        DatabaseObjectFactory factory = DatabaseObjectFactory.getInstance(project);
        factory.openFactoryInputDialog(schema, objectList.getObjectType());
    }

    @NotNull
    public DBObjectList getObjectList() {
        return Failsafe.nn(WeakRef.get(objectList));
    }
}