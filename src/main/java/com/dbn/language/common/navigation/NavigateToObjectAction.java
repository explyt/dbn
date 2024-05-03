package com.dbn.language.common.navigation;

import com.dbn.common.action.BasicAction;
import com.dbn.object.common.DBObject;
import com.dbn.object.lookup.DBObjectRef;
import com.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class NavigateToObjectAction extends BasicAction {
    private final DBObjectRef object;
    public NavigateToObjectAction(DBObject object, DBObjectType objectType) {
        super("Navigate to " + objectType.getName(), null, objectType.getIcon());
        this.object = DBObjectRef.of(object);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (object != null) {
            DBObject object = this.object.get();
            if (object != null) {
                object.navigate(true);
            }
        }

    }
}