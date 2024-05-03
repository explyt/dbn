package com.dbn.execution.method.action;

import com.dbn.common.icon.Icons;
import com.dbn.debugger.DatabaseDebuggerManager;
import com.dbn.object.DBMethod;
import com.dbn.object.DBProgram;
import com.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodDebugAction extends AnObjectAction<DBMethod> {
    public MethodDebugAction(DBMethod method) {
        super(method);
    }

    MethodDebugAction(DBProgram program, DBMethod method) {
        super(method);
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DBMethod object) {

        DatabaseDebuggerManager executionManager = DatabaseDebuggerManager.getInstance(project);
        executionManager.startMethodDebugger(object);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable DBMethod target) {
        presentation.setText("Debug...");
        presentation.setIcon(Icons.METHOD_EXECUTION_DEBUG);
    }
}
