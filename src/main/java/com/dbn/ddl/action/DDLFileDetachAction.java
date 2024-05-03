package com.dbn.ddl.action;

import com.dbn.common.action.ProjectAction;
import com.dbn.ddl.DDLFileAttachmentManager;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DDLFileDetachAction extends ProjectAction {
    private final DBObjectRef<DBSchemaObject> object;

    public DDLFileDetachAction(DBSchemaObject object) {
        this.object = DBObjectRef.of(object);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        fileAttachmentManager.detachDDLFiles(object);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        boolean hasAttachedDDLFiles = fileAttachmentManager.hasAttachedDDLFiles(object);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(hasAttachedDDLFiles);
        presentation.setText("Detach Files");
    }
}