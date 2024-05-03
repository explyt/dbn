package com.dbn.generator.action;

import com.dbn.connection.ConnectionHandler;
import com.dbn.generator.StatementGenerationManager;
import com.dbn.generator.StatementGeneratorResult;
import com.dbn.object.DBTable;
import com.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GenerateInsertStatementAction extends GenerateStatementAction {
    private DBObjectRef<DBTable> table;

    GenerateInsertStatementAction(DBTable table) {
        this.table = DBObjectRef.of(table);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        e.getPresentation().setText("INSERT Statement");
    }

    @Override
    protected StatementGeneratorResult generateStatement(Project project) {
        StatementGenerationManager statementGenerationManager = StatementGenerationManager.getInstance(project);
        DBTable table = getTable();
        return statementGenerationManager.generateInsert(table);
    }

    @NotNull
    private DBTable getTable() {
        return DBObjectRef.ensure(table);
    }

    @Nullable
    @Override
    public ConnectionHandler getConnection() {
        return getTable().getConnection();
    }
}
