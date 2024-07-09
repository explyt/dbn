package com.dbn.execution.explain.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.common.ui.ExecutionStatementViewerPopup;
import com.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ExplainPlanResultViewStatementAction extends AbstractExplainPlanResultAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull ExplainPlanResult explainPlanResult) {
        ExecutionStatementViewerPopup statementViewer = new ExecutionStatementViewerPopup(explainPlanResult);
        statementViewer.show((Component) e.getInputEvent().getSource());
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Presentation presentation, @NotNull Project project, @Nullable ExplainPlanResult target) {
        presentation.setText("View SQL Statement");
        presentation.setIcon(Icons.EXEC_RESULT_VIEW_STATEMENT);
    }
}
