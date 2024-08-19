package com.dbn.execution;

import com.dbn.common.ui.window.DBNToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.icon.Icons.WINDOW_EXECUTION_CONSOLE;

public class ExecutionConsoleToolWindowFactory extends DBNToolWindowFactory {
    @Override
    public void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("DB Execution Console");
        toolWindow.setStripeTitle("DB Execution Console");
        toolWindow.setIcon(WINDOW_EXECUTION_CONSOLE.get());
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setAutoHide(false);
        toolWindow.setAvailable(false, null);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }
}
