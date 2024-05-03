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

public class ObjectEditCodeAction extends ProjectAction {
    private final DBObjectRef<DBSchemaObject> object;

    ObjectEditCodeAction(DBSchemaObject object) {
        this.object = DBObjectRef.of(object);
        setDefaultIcon(true);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Edit Code");
        presentation.setIcon(Icons.OBEJCT_EDIT_SOURCE);
    }

    @Nullable
    @Override
    public  Project getProject() {
        return getObject().getProject();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DBSchemaObject schemaObject = getObject();
        DatabaseFileEditorManager editorManager = DatabaseFileEditorManager.getInstance(project);
        editorManager.connectAndOpenEditor(schemaObject, EditorProviderId.CODE, false, true);
    }

    @NotNull
    private DBSchemaObject getObject() {
        return DBObjectRef.ensure(object);
    }
}
