package com.dbn.common.ui.window;

import com.dbn.common.event.ProjectEvents;
import com.dbn.common.icon.IconBundle;
import com.dbn.common.ui.util.UserInterface;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;


public abstract class DBNToolWindowFactory implements ToolWindowFactory, DumbAware {
    private final IconBundle icons;

    public DBNToolWindowFactory() {
        icons = createIconBundle();
    }

    @Override
    public final void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        createContent(project, toolWindow);
        toolWindow.setIcon(icons.getIcon(false, UserInterface.isCompactMode()));
        addSelectionListener(project, toolWindow);
    }

    protected abstract IconBundle createIconBundle();

    protected abstract void createContent(@NotNull Project project, @NotNull ToolWindow toolWindow);

    private void addSelectionListener(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        if (!UserInterface.isNewUI()) return;
        ProjectEvents.subscribe(project, null,
                ToolWindowManagerListener.TOPIC,
                createSelectionListener(toolWindow));
    }

    private @NotNull ToolWindowManagerListener createSelectionListener(@NotNull ToolWindow toolWindow) {
        return new ToolWindowManagerListener() {
            @Override
            public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
                // replace icon with color-neutral version if selected
                String activeWindowId = toolWindowManager.getActiveToolWindowId();
                String windowId = toolWindow.getId();

                boolean selected = Objects.equals(windowId, activeWindowId);
                Icon icon = icons.getIcon(selected, UserInterface.isCompactMode());

                toolWindow.setIcon(icon);            }
        };
    }
}
