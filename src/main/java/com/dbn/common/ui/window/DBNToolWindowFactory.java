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
        //addSettingsListener(project, toolWindow);
        //toolWindow.setIcon(resolveIcon());
    }


    protected abstract void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow);
}
