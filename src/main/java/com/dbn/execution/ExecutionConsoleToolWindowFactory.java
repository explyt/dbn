package com.dbn.execution;

import com.dbn.common.icon.Icons;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ExecutionConsoleToolWindowFactory implements ToolWindowFactory, DumbAware{
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("DB Execution Console");
        toolWindow.setStripeTitle("DB Execution Console");
        toolWindow.setIcon(Icons.WINDOW_EXECUTION_CONSOLE);
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setAutoHide(false);
        toolWindow.setAvailable(false, null);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return false;
    }

    @Nullable
    //@Override
    public Icon getIcon() {
        return Icons.WINDOW_EXECUTION_CONSOLE;
    }

}
