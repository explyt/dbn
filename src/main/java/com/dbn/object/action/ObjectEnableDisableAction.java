package com.dbn.object.action;

import com.dbn.common.thread.Progress;
import com.dbn.common.util.Messages;
import com.dbn.object.common.DBSchemaObject;
import com.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dbn.object.common.operation.DBOperationType;
import com.dbn.object.common.status.DBObjectStatus;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.dbn.common.dispose.Checks.isValid;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

public class ObjectEnableDisableAction extends AnObjectAction<DBSchemaObject> {
    ObjectEnableDisableAction(DBSchemaObject object) {
        super(object);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DBSchemaObject object) {

        boolean enabled = object.getStatus().is(DBObjectStatus.ENABLED);
        String action = enabled ? "Disabling" : "Enabling";
        Progress.prompt(project, object, false,
                action + " object",
                action + " " + object.getQualifiedNameWithType(),
                progress -> {
                    try {
                        DBOperationType operationType = enabled ? DBOperationType.DISABLE : DBOperationType.ENABLE;
                        object.getOperationExecutor().executeOperation(operationType);
                    } catch (SQLException e1) {
                        conditionallyLog(e1);
                        String message = "Error " + (!enabled ? "enabling " : "disabling ") + object.getQualifiedNameWithType();
                        Messages.showErrorDialog(project, message, e1);
                    } catch (DBOperationNotSupportedException e1) {
                        conditionallyLog(e1);
                        Messages.showErrorDialog(project, e1.getMessage());
                    }
                });
    }

    @Override
    protected void update(
            @NotNull AnActionEvent e,
            @NotNull Presentation presentation,
            @NotNull Project project,
            @Nullable DBSchemaObject target) {

        if (isValid(target)) {
            boolean enabled = target.getStatus().is(DBObjectStatus.ENABLED);
            presentation.setText(!enabled ? "Enable" : "Disable");
            presentation.setVisible(true);
        } else {
            presentation.setVisible(false);
        }
    }
}