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

public class ObjectEditDataAction extends ProjectAction {
    private final DBObjectRef<DBSchemaObject> object;

    public ObjectEditDataAction(DBSchemaObject object) {
        this.object = DBObjectRef.of(object);
        setDefaultIcon(true);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Edit Data");
        presentation.setIcon(Icons.OBJECT_EDIT_DATA);
    }

    public DBSchemaObject getObject() {
        return DBObjectRef.ensure(object);
    }

    @Nullable
    @Override
    public Project getProject() {
        return getObject().getProject();
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DBSchemaObject object = getObject();
        DatabaseFileEditorManager editorManager = DatabaseFileEditorManager.getInstance(project);
        editorManager.connectAndOpenEditor(object, EditorProviderId.DATA, false, true);
    }
}