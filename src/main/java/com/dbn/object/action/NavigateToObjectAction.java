package com.dbn.object.action;

import com.dbn.common.action.BasicAction;
import com.dbn.object.common.DBObject;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class NavigateToObjectAction extends BasicAction {
    private final DBObjectRef<DBObject> objectRef;

    public NavigateToObjectAction(DBObject object) {
        super();
        Presentation presentation = getTemplatePresentation();
        presentation.setText(object.getName(), false);
        presentation.setIcon(object.getIcon());
        this.objectRef = DBObjectRef.of(object);
    }

    public NavigateToObjectAction(DBObject sourceObject, DBObject object) {
        super();
        this.objectRef = DBObjectRef.of(object);

        Presentation presentation = getTemplatePresentation();
        presentation.setText(
                sourceObject != object.getParentObject() ?
                        object.getQualifiedName() :
                        object.getName(), false);
        presentation.setIcon(object.getIcon());
        presentation.setDescription(object.getTypeName());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DBObjectRef.ensure(objectRef).navigate(true);
    }
}
