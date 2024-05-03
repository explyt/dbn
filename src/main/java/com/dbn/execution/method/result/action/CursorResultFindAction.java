package com.dbn.execution.method.result.action;

import com.dbn.common.icon.Icons;
import com.dbn.execution.method.result.ui.MethodExecutionCursorResultForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.dispose.Checks.isValid;

public class CursorResultFindAction extends MethodExecutionCursorResultAction {

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Find Data");
        presentation.setIcon(Icons.ACTION_FIND);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        MethodExecutionCursorResultForm cursorResultForm = getCursorResultForm(e);
        if (isValid(cursorResultForm)) {
            cursorResultForm.showSearchHeader();
        }
    }
}
