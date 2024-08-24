package com.dbn.common.ui.window;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;


public abstract class DBNToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public final void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        createContent(project, toolWindow);
    }

    @Override
    public final void init(@NotNull ToolWindow toolWindow) {
        ToolWindowFactory.super.init(toolWindow);
        initialize(toolWindow);
    }

    /**
     * Initializer for icon, title and availability
     * @param toolWindow the {@link ToolWindow} the initialization should be performed against
     */
    protected abstract void initialize(@NotNull ToolWindow toolWindow);

    protected abstract void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow);
}
