package com.dbn.browser;

import com.dbn.browser.ui.BrowserToolWindowForm;
import com.dbn.common.compatibility.Compatibility;
import com.dbn.common.icon.Icons;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DatabaseBrowserToolWindowFactory implements ToolWindowFactory, DumbAware{
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        BrowserToolWindowForm toolWindowForm = browserManager.getToolWindowForm();

        ContentManager contentManager = toolWindow.getContentManager();
        ContentFactory contentFactory = contentManager.getFactory();
        Content content = contentFactory.createContent(toolWindowForm.getComponent(), null, true);

        toolWindow.setTitle("DB Browser");
        toolWindow.setStripeTitle("DB Browser");
        toolWindow.setIcon(Icons.WINDOW_DATABASE_BROWSER);
        contentManager.addContent(content);
    }

    @Nullable
    //@Override
    public Icon getIcon() {
        return Icons.WINDOW_DATABASE_BROWSER;
    }
}
