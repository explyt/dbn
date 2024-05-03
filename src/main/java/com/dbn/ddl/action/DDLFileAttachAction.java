package com.dbn.ddl.action;

import com.dbn.ddl.DDLFileAttachmentManager;
import com.dbn.object.action.AnObjectAction;
import com.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DDLFileAttachAction extends AnObjectAction<DBSchemaObject> {
    public DDLFileAttachAction(@NotNull DBSchemaObject object) {
        super(object);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DBSchemaObject target) {
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
        fileAttachmentManager.attachDDLFiles(target.ref());
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DBSchemaObject target) {
        presentation.setText("Attach Files");
    }
}
