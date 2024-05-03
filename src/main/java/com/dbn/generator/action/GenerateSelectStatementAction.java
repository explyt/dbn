package com.dbn.generator.action;

import com.dbn.connection.ConnectionHandler;
import com.dbn.generator.StatementGenerationManager;
import com.dbn.generator.StatementGeneratorResult;
import com.dbn.object.common.DBObject;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenerateSelectStatementAction extends GenerateStatementAction {
    private final List<DBObjectRef<DBObject>> selectedObjectRefs;

    GenerateSelectStatementAction(List<DBObject> selectedObjects) {
        this.selectedObjectRefs = DBObjectRef.from(selectedObjects);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setText("SELECT Statement");
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGenerationManager statementGenerationManager = StatementGenerationManager.getInstance(project);
        List<DBObject> selectedObjects = getSelectedObjects();
        return statementGenerationManager.generateSelectStatement(selectedObjects, true);
    }

    private List<DBObject> getSelectedObjects() {
        return DBObjectRef.ensure(selectedObjectRefs);
    }

    @Nullable
    @Override
    public ConnectionHandler getConnection() {
        List<DBObject> selectedObjects = getSelectedObjects();
        if (selectedObjects.size() > 0) {
            return selectedObjects.get(0).getConnection();
        }
        return null;
    }
}
