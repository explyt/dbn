package com.dbn.execution;

import com.dbn.common.icon.IconBundle;
import com.dbn.common.icon.Icons;
import com.dbn.common.ui.window.DBNToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

public class ExecutionConsoleToolWindowFactory extends DBNToolWindowFactory {
    @Override
    public void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("DB Execution Console");
        toolWindow.setStripeTitle("DB Execution Console");
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setAutoHide(false);
        toolWindow.setAvailable(false, null);
    }

    @Override
    protected IconBundle createIconBundle() {
        return new IconBundle(
                Icons.WINDOW_EXECUTION_CONSOLE,
                Icons.WINDOW_EXECUTION_CONSOLE_S,
                Icons.WINDOW_EXECUTION_CONSOLE_L,
                Icons.WINDOW_EXECUTION_CONSOLE_LS);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }
}
